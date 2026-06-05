package com.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.*;
import com.ecommerce.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Autowired private AdminService adminService;
    @Autowired private UserService userService;
    @Autowired private ProductService productService;
    @Autowired private OrderService orderService;
    @Autowired private ReviewService reviewService;
    @Autowired private BannerService bannerService;
    @Autowired private AnnouncementService announcementService;
    @Autowired private CouponService couponService;
    @Autowired private FeedbackService feedbackService;
    @Autowired private MessageService messageService;

    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private BannerMapper bannerMapper;
    @Autowired private AnnouncementMapper announcementMapper;
    @Autowired private CouponMapper couponMapper;
    @Autowired private FeedbackMapper feedbackMapper;
    @Autowired private CartItemMapper cartItemMapper;
    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private MessageMapper messageMapper;
    @Autowired private FlashSaleMapper flashSaleMapper;
    @Autowired private FlashSaleService flashSaleService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    // ===== Dashboard =====
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }

    // ===== Users =====
    @PostMapping("/users/backfill-uids")
    public ApiResponse<Map<String, Object>> backfillUids() {
        // Ensure the uid column exists in the database
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN uid VARCHAR(3) UNIQUE");
        } catch (Exception e) {
            // Column already exists — ignore
        }
        int count = userService.backfillUids();
        Map<String, Object> data = new HashMap<>();
        data.put("updated", count);
        return ApiResponse.ok("UID补全完成", data);
    }

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            @RequestParam(required = false) String keyword) {
        Page<User> result = userService.page(page, page_size, keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @PostMapping("/users")
    public ApiResponse<User> userCreate(@RequestBody Map<String, String> body) {
        User user = new User();
        user.setUsername(body.get("username"));
        user.setPasswordHash(body.get("password"));
        user.setEmail(body.getOrDefault("email", ""));
        user.setPhone(body.getOrDefault("phone", ""));
        user = userService.create(user);
        user.setPasswordHash(null);
        return ApiResponse.ok("创建成功", user);
    }

    @GetMapping("/users/{id}")
    public ApiResponse<User> userDetail(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user != null) user.setPasswordHash(null);
        return ApiResponse.ok(user);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<?> userUpdate(@PathVariable Integer id, @RequestBody User data) {
        data.setId(id);
        userService.update(data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<?> userDelete(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }
        // Cascade cleanup: delete all dependent records
        addressMapper.delete(new QueryWrapper<Address>().eq("user_id", id));
        cartItemMapper.delete(new QueryWrapper<CartItem>().eq("user_id", id));
        favoriteMapper.delete(new QueryWrapper<Favorite>().eq("user_id", id));
        feedbackMapper.delete(new QueryWrapper<Feedback>().eq("user_id", id));
        messageMapper.delete(new QueryWrapper<Message>().eq("from_user_id", id)
                .or().eq("to_user_id", id));
        // Handle orders: delete order items then orders
        List<Order> userOrders = orderMapper.selectList(
                new QueryWrapper<Order>().eq("user_id", id));
        for (Order o : userOrders) {
            orderItemMapper.delete(new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
        }
        orderMapper.delete(new QueryWrapper<Order>().eq("user_id", id));
        // Delete reviews
        reviewMapper.delete(new QueryWrapper<Review>().eq("user_id", id));
        // Finally delete the user
        userMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<?> userStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        User user = userService.findById(id);
        if (user != null) {
            user.setStatus(body.get("status"));
            userService.update(user);
        }
        return ApiResponse.ok(null);
    }

    // ===== Categories =====
    @GetMapping("/categories")
    public ApiResponse<List<Category>> categoryList() {
        List<Category> all = categoryMapper.selectList(
                new QueryWrapper<Category>().orderByDesc("id"));
        // Build tree
        List<Category> roots = new ArrayList<>();
        Map<Integer, List<Category>> childrenMap = new HashMap<>();
        for (Category c : all) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(c);
            } else {
                childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
            }
        }
        for (Category c : all) {
            c.setChildren(childrenMap.get(c.getId()));
        }
        return ApiResponse.ok(roots);
    }

    @PostMapping("/categories")
    public ApiResponse<Category> categoryCreate(@RequestBody Category category) {
        categoryMapper.insert(category);
        return ApiResponse.ok(category);
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<?> categoryUpdate(@PathVariable Integer id, @RequestBody Category data) {
        data.setId(id);
        categoryMapper.updateById(data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<?> categoryDelete(@PathVariable Integer id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        // Check for child categories
        Long childCount = categoryMapper.selectCount(
                new QueryWrapper<Category>().eq("parent_id", id));
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，请先删除子分类");
        }
        // Check for products in this category
        Long productCount = productMapper.selectCount(
                new QueryWrapper<Product>().eq("category_id", id));
        if (productCount > 0) {
            throw new BusinessException("该分类下有 " + productCount + " 件商品，请先移除商品");
        }
        categoryMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ===== Products =====
    @GetMapping("/products")
    public ApiResponse<Map<String, Object>> productList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer category_id,
            @RequestParam(required = false) String status) {
        Page<Product> result = productService.adminList(page, page_size, keyword, category_id, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @PostMapping("/products")
    public ApiResponse<Map<String, Object>> productCreate(
            @RequestParam Map<String, String> params,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        Product product = buildProduct(params, images);
        validateProduct(product);
        productMapper.insert(product);
        Map<String, Object> data = new HashMap<>();
        data.put("images", product.getImages());
        return ApiResponse.ok("创建成功", data);
    }

    @PostMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> productUpdate(
            @PathVariable Integer id,
            @RequestParam Map<String, String> params,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        Product existing = productMapper.selectById(id);
        if (existing == null) return ApiResponse.error(400, "商品不存在");
        Product incoming = buildProduct(params, images);
        // Preserve fields that should not be overwritten by updates
        incoming.setId(id);
        incoming.setSales(existing.getSales());
        incoming.setCreatedAt(existing.getCreatedAt());
        // If no new images and no keep_images specified, keep existing images
        if (!params.containsKey("keep_images") && (images == null || images.isEmpty())) {
            incoming.setImages(existing.getImages());
        }
        productMapper.updateById(incoming);
        Map<String, Object> data = new HashMap<>();
        data.put("images", incoming.getImages());
        return ApiResponse.ok("更新成功", data);
    }

    @PostMapping("/products/{id}/upload-images")
    public ApiResponse<Product> productUploadImages(
            @PathVariable Integer id,
            @RequestParam("images") List<MultipartFile> images) throws IOException {

        Product product = productMapper.selectById(id);
        if (product == null) {
            return ApiResponse.error(400, "商品不存在");
        }
        if (images == null || images.isEmpty()) {
            return ApiResponse.error(400, "请至少上传一张图片");
        }

        List<String> imageList = product.getImages();
        if (imageList == null) {
            imageList = new ArrayList<>();
        }

        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;
            String ext = getExt(file.getOriginalFilename());
            if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                return ApiResponse.error(400, "仅支持 JPG/PNG/GIF/WebP 格式");
            }
            String filename = System.currentTimeMillis() + "-" + new Random().nextInt(10000) + ext;
            file.transferTo(new File(uploadPath, filename));
            imageList.add("/uploads/" + filename);
        }

        product.setImages(imageList);
        product.setUpdatedAt(new Date());
        productMapper.updateById(product);

        return ApiResponse.ok("上传成功", product);
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<?> productDelete(@PathVariable Integer id) {
        productService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/products/template")
    public ResponseEntity<byte[]> productTemplate() throws Exception {
        byte[] data = adminService.exportProductTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @PostMapping("/products/import")
    public ApiResponse<Map<String, Object>> productImport(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> result = adminService.importProducts(file);
        return ApiResponse.ok("导入完成", result);
    }

    private Product buildProduct(Map<String, String> params, List<MultipartFile> files) throws IOException {
        Product p = new Product();
        p.setName(params.get("name"));
        p.setDescription(params.getOrDefault("description", ""));
        p.setDetail(params.getOrDefault("detail", ""));
        try { p.setCategoryId(params.containsKey("category_id") ? Integer.parseInt(params.get("category_id")) : null); }
        catch (NumberFormatException e) { p.setCategoryId(null); }
        p.setPrice(new BigDecimal(params.getOrDefault("price", "0")));
        if (params.containsKey("original_price") && !params.get("original_price").isEmpty()) {
            try { p.setOriginalPrice(new BigDecimal(params.get("original_price"))); }
            catch (NumberFormatException e) { /* ignore */ }
        }
        try { p.setStock(Integer.parseInt(params.getOrDefault("stock", "0"))); }
        catch (NumberFormatException e) { p.setStock(0); }
        p.setStatus(params.getOrDefault("status", "on"));
        p.setIsNew("1".equals(params.get("is_new")) || "true".equals(params.get("is_new")));
        p.setIsHot("1".equals(params.get("is_hot")) || "true".equals(params.get("is_hot")));
        p.setIsPromotion("1".equals(params.get("is_promotion")) || "true".equals(params.get("is_promotion")));

        // Handle specs JSON
        if (params.containsKey("specs") && !params.get("specs").isEmpty()) {
            try {
                p.setSpecs(new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        params.get("specs"), new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}));
            } catch (Exception ignored) {}
        }

        // Handle tags JSON
        if (params.containsKey("tags") && !params.get("tags").isEmpty()) {
            try {
                p.setTags(new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        params.get("tags"), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
            } catch (Exception ignored) {}
        } else {
            p.setTags(new ArrayList<>());
        }

        // Images: keep existing + new uploads
        List<String> imageList = new ArrayList<>();
        if (params.containsKey("keep_images")) {
            try {
                List<String> kept = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        params.get("keep_images"), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                imageList.addAll(kept);
            } catch (Exception ignored) {}
        }
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String ext = getExt(file.getOriginalFilename());
                    String filename = System.currentTimeMillis() + "-" + new Random().nextInt(10000) + ext;
                    file.transferTo(new File(uploadPath, filename));
                    imageList.add("/uploads/" + filename);
                }
            }
        }
        p.setImages(imageList);
        return p;
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }

    private void deleteUploadFile(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) return;
        String filename = imageUrl.substring("/uploads/".length());
        File file = new File(uploadPath, filename);
        if (file.exists()) {
            file.delete();
        }
    }

    private void validateProduct(Product p) {
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            throw new BusinessException("商品名称不能为空");
        }
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于0");
        }
        if (p.getStock() < 0) {
            throw new BusinessException("商品库存不能为负数");
        }
    }

    // ===== Orders =====
    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> orderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String order_no,
            @RequestParam(required = false) String status) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        if (order_no != null && !order_no.isEmpty()) qw.like("order_no", order_no);
        if (status != null && !status.isEmpty()) qw.eq("status", status);
        qw.orderByDesc("created_at");
        Page<Order> result = orderMapper.selectPage(new Page<>(page, limit), qw);
        for (Order o : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
            o.setItems(items);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Order> orderDetail(@PathVariable Integer id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", id));
            for (OrderItem item : items) {
                Product p = productMapper.selectById(item.getProductId());
                if (p != null) p.setCategory(null);
                item.setProduct(p);
            }
            order.setItems(items);
        }
        return ApiResponse.ok(order);
    }

    @PutMapping("/orders/{id}/ship")
    public ApiResponse<?> orderShip(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Order order = orderMapper.selectById(id);
        if (order == null) return ApiResponse.error(400, "订单不存在");
        if (!"pending_shipment".equals(order.getStatus())) {
            return ApiResponse.error(400, "当前订单状态不可发货");
        }
        order.setStatus("shipped");
        order.setShippingTime(new Date());
        String trackingNo = body.getOrDefault("tracking_no", "");
        if (trackingNo.isEmpty()) {
            trackingNo = "EXP" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        }
        order.setTrackingNo(trackingNo);
        order.setShippingCompany(body.getOrDefault("company", ""));
        orderMapper.updateById(order);
        return ApiResponse.ok(null);
    }

    @Transactional
    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<?> orderCancel(@PathVariable Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) return ApiResponse.error(400, "订单不存在");
        if ("cancelled".equals(order.getStatus()) || "completed".equals(order.getStatus())
                || "refunding".equals(order.getStatus()) || "refunded".equals(order.getStatus())
                || "shipped".equals(order.getStatus())) {
            return ApiResponse.error(400, "当前状态不可取消");
        }
        // Restore stock for pending_payment and pending_shipment (stock deducted at creation)
        if ("pending_payment".equals(order.getStatus()) || "pending_shipment".equals(order.getStatus())) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", id));
            for (OrderItem oi : items) {
                productMapper.restoreStock(oi.getProductId(), oi.getQuantity());
                // Restore flash sale stock
                Map<String, Object> snapshot = oi.getProductSnapshot();
                if (snapshot != null && snapshot.get("flashSaleId") != null) {
                    flashSaleMapper.restoreStock((Integer) snapshot.get("flashSaleId"), oi.getQuantity());
                }
            }
            if (order.getCouponId() != null) {
                couponMapper.restoreAtomic(order.getCouponId());
            }
        }
        // CAS status transition
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", id).in("status", "pending_payment", "pending_shipment");
        uw.set("status", "cancelled");
        int affected = orderMapper.update(null, uw);
        if (affected == 0) return ApiResponse.error(400, "取消失败，订单状态已变更");
        return ApiResponse.ok(null);
    }

    @Transactional
    @PutMapping("/orders/{id}/refund")
    public ApiResponse<?> orderRefund(@PathVariable Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) return ApiResponse.error(400, "订单不存在");
        if (!"completed".equals(order.getStatus()) && !"refunding".equals(order.getStatus())) {
            return ApiResponse.error(400, "当前订单状态不可退款");
        }
        // Restore stock
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        for (OrderItem oi : items) {
            productMapper.restoreStock(oi.getProductId(), oi.getQuantity());
            // Restore flash sale stock
            Map<String, Object> snapshot = oi.getProductSnapshot();
            if (snapshot != null && snapshot.get("flashSaleId") != null) {
                flashSaleMapper.restoreStock((Integer) snapshot.get("flashSaleId"), oi.getQuantity());
            }
        }
        // Restore coupon
        if (order.getCouponId() != null) {
            couponMapper.restoreAtomic(order.getCouponId());
        }
        // CAS status transition
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", id).in("status", "completed", "refunding");
        uw.set("status", "refunded");
        int affected = orderMapper.update(null, uw);
        if (affected == 0) return ApiResponse.error(400, "退款失败，订单状态已变更");
        return ApiResponse.ok(null);
    }

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> orderExport() throws Exception {
        byte[] data = adminService.exportOrders();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    // ===== Reviews =====
    @GetMapping("/reviews")
    public ApiResponse<Map<String, Object>> reviewList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer product_id) {
        Page<Review> result = reviewService.adminList(page, limit, product_id);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<?> reviewDelete(@PathVariable Integer id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ===== Banners =====
    @GetMapping("/banners")
    public ApiResponse<List<Banner>> bannerList() {
        return ApiResponse.ok(bannerService.listAll());
    }

    @PostMapping("/banners")
    public ApiResponse<Banner> bannerCreate(@RequestBody Banner banner) {
        banner.setCreatedAt(new Date());
        banner.setUpdatedAt(new Date());
        if (banner.getStatus() == null) banner.setStatus("on");
        bannerMapper.insert(banner);
        return ApiResponse.ok(banner);
    }

    @PutMapping("/banners/{id}")
    public ApiResponse<?> bannerUpdate(@PathVariable Integer id, @RequestBody Banner data) {
        data.setId(id);
        data.setUpdatedAt(new Date());
        bannerMapper.updateById(data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/banners/{id}")
    public ApiResponse<?> bannerDelete(@PathVariable Integer id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }
        // Delete the image file from disk
        deleteUploadFile(banner.getImageUrl());
        bannerMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/banners/upload-image")
    public ApiResponse<Map<String, String>> bannerUploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "请选择图片文件");
        }
        String ext = getExt(file.getOriginalFilename());
        String filename = System.currentTimeMillis() + "-" + new Random().nextInt(10000) + ext;
        file.transferTo(new File(uploadPath, filename));
        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/" + filename);
        return ApiResponse.ok("上传成功", data);
    }

    // ===== Announcements =====
    @GetMapping("/announcements")
    public ApiResponse<List<Announcement>> annoList() {
        return ApiResponse.ok(announcementService.listAll());
    }

    @PostMapping("/announcements")
    public ApiResponse<Announcement> annoCreate(@RequestBody Announcement anno) {
        anno.setCreatedAt(new Date());
        anno.setUpdatedAt(new Date());
        if (anno.getStatus() == null) anno.setStatus("on");
        announcementMapper.insert(anno);
        return ApiResponse.ok(anno);
    }

    @PutMapping("/announcements/{id}")
    public ApiResponse<?> annoUpdate(@PathVariable Integer id, @RequestBody Announcement data) {
        data.setId(id);
        data.setUpdatedAt(new Date());
        announcementMapper.updateById(data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResponse<?> annoDelete(@PathVariable Integer id) {
        Announcement anno = announcementMapper.selectById(id);
        if (anno == null) {
            throw new BusinessException("公告不存在");
        }
        announcementMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ===== Coupons =====
    @GetMapping("/coupons")
    public ApiResponse<List<Coupon>> couponList() {
        return ApiResponse.ok(couponService.listAll());
    }

    @PostMapping("/coupons")
    public ApiResponse<Coupon> couponCreate(@RequestBody Coupon coupon) {
        coupon.setCreatedAt(new Date());
        coupon.setUpdatedAt(new Date());
        if (coupon.getStatus() == null) coupon.setStatus("active");
        if (coupon.getUsed() == null) coupon.setUsed(0);
        couponMapper.insert(coupon);
        return ApiResponse.ok(coupon);
    }

    @PutMapping("/coupons/{id}")
    public ApiResponse<?> couponUpdate(@PathVariable Integer id, @RequestBody Coupon data) {
        data.setId(id);
        data.setUpdatedAt(new Date());
        couponMapper.updateById(data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/coupons/{id}")
    public ApiResponse<?> couponDelete(@PathVariable Integer id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        couponMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ===== Flash Sales =====
    @GetMapping("/flashsales")
    public ApiResponse<List<FlashSale>> flashSaleList() {
        List<FlashSale> list = flashSaleMapper.selectList(
                new QueryWrapper<FlashSale>().orderByDesc("start_time"));
        for (FlashSale fs : list) {
            Product p = productMapper.selectById(fs.getProductId());
            if (p != null) { p.setCategory(null); fs.setProduct(p); }
        }
        return ApiResponse.ok(list);
    }

    @PostMapping("/flashsales")
    public ApiResponse<FlashSale> flashSaleCreate(@RequestBody FlashSale data) {
        data.setSold(0);
        data.setCreatedAt(new Date());
        data.setUpdatedAt(new Date());
        if (data.getStatus() == null) data.setStatus("active");
        flashSaleMapper.insert(data);
        return ApiResponse.ok(data);
    }

    @PutMapping("/flashsales/{id}")
    public ApiResponse<?> flashSaleUpdate(@PathVariable Integer id, @RequestBody FlashSale data) {
        data.setId(id);
        data.setUpdatedAt(new Date());
        flashSaleMapper.updateById(data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/flashsales/{id}")
    public ApiResponse<?> flashSaleDelete(@PathVariable Integer id) {
        FlashSale fs = flashSaleMapper.selectById(id);
        if (fs == null) throw new BusinessException("秒杀活动不存在");
        flashSaleMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    // ===== Feedbacks =====
    @GetMapping("/feedbacks")
    public ApiResponse<Map<String, Object>> feedbackList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Feedback> result = feedbackService.adminList(page, limit);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @PutMapping("/feedbacks/{id}/reply")
    public ApiResponse<?> feedbackReply(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        feedbackService.reply(id, body.get("reply"));
        return ApiResponse.ok(null);
    }

    // ===== Messages (客服) =====
    @GetMapping("/messages/conversations")
    public ApiResponse<Map<String, Object>> messageConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(messageService.adminConversations(page, limit));
    }

    @GetMapping("/messages/{userId}")
    public ApiResponse<List<Message>> messageDetail(@PathVariable Integer userId) {
        return ApiResponse.ok(messageService.getConversation(userId));
    }

    @PostMapping("/messages/{userId}/reply")
    public ApiResponse<Message> messageReply(@PathVariable Integer userId,
                                              @RequestBody Map<String, String> body,
                                              HttpServletRequest req) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "回复内容不能为空");
        }
        Integer adminId = (Integer) req.getAttribute("userId");
        Message msg = messageService.send(adminId, userId, content);
        return ApiResponse.ok(msg);
    }

    // ===== Admin Profile =====
    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> adminProfile(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        User user = userService.findById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        return ApiResponse.ok(data);
    }

    @PutMapping("/profile/password")
    public ApiResponse<?> adminChangePassword(@RequestBody Map<String, String> body,
                                               HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        String oldPassword = body.get("old_password");
        String newPassword = body.get("new_password");
        User user = userService.findById(userId);
        if (!userService.verifyPassword(user, oldPassword)) {
            return ApiResponse.error(400, "原密码错误");
        }
        userService.updatePassword(userId, newPassword);
        return ApiResponse.ok(null);
    }
}
