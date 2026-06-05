package com.ecommerce.seed;

import com.ecommerce.entity.*;
import com.ecommerce.mapper.*;
import com.ecommerce.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private BannerMapper bannerMapper;
    @Autowired private AnnouncementMapper announcementMapper;
    @Autowired private CouponMapper couponMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ReviewMapper reviewMapper;
    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private FeedbackMapper feedbackMapper;
    @Autowired private CartItemMapper cartItemMapper;
    @Autowired private FlashSaleMapper flashSaleMapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Random rand = new Random();

    @Override
    public void run(String... args) {
        // Auto-repair missing columns (schema drift fix)
        repairSchema();

        long userCount = userMapper.selectCount(null);
        if (userCount > 0) {
            System.out.println("[Seeder] Data already exists (" + userCount + " users), skipping seed.");
            return;
        }
        System.out.println("[Seeder] Seeding empty database...");

        // Clear existing data in reverse dependency order
        cartItemMapper.delete(null);
        favoriteMapper.delete(null);
        orderItemMapper.delete(null);
        orderMapper.delete(null);
        addressMapper.delete(null);
        reviewMapper.delete(null);
        feedbackMapper.delete(null);
        flashSaleMapper.delete(null);
        productMapper.delete(null);
        categoryMapper.delete(null);
        bannerMapper.delete(null);
        announcementMapper.delete(null);
        couponMapper.delete(null);
        userMapper.delete(null);

        seedUsers();
        seedCategories();
        seedProducts();
        seedBanners();
        seedAnnouncements();
        seedCoupons();
        seedAddresses();
        seedOrders();
        seedReviews();
        seedFavorites();
        seedFeedbacks();
        seedCartItems();
        seedFlashSales();

        System.out.println("[Seeder] Seed complete! Admin: admin/admin123, User: user1/123456");
    }

    private void repairSchema() {
        String[][] alters = {
            {"favorites", "ALTER TABLE favorites ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"},
            {"order_items", "ALTER TABLE order_items ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"},
            {"reviews", "ALTER TABLE reviews ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"}
        };
        for (String[] entry : alters) {
            try {
                jdbcTemplate.execute(entry[1]);
                System.out.println("[Seeder] Schema repair: added updated_at to " + entry[0]);
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate column")) {
                    System.out.println("[Seeder] Schema OK: " + entry[0] + " already has updated_at");
                } else {
                    System.out.println("[Seeder] Schema repair error on " + entry[0] + ": " + e.getMessage());
                }
            }
        }
    }

    private void seedUsers() {
        String hash = passwordEncoder.encode("admin123");
        User admin = new User();
        admin.setUsername("admin"); admin.setEmail("admin@shop.com");
        admin.setPasswordHash(hash); admin.setPhone("13800000001");
        admin.setRole("admin"); admin.setStatus("active");
        admin.setAvatar("/default-avatar.png");
        userMapper.insert(admin);
        admin.setUid(String.format("%03d", admin.getId()));
        userMapper.updateById(admin);

        String userHash = passwordEncoder.encode("123456");
        for (int i = 1; i <= 20; i++) {
            User u = new User();
            u.setUsername("user" + i);
            u.setEmail("user" + i + "@test.com");
            u.setPasswordHash(userHash);
            u.setPhone("1380000" + String.format("%04d", i));
            u.setRole("user"); u.setStatus("active");
            u.setAvatar("/default-avatar.png");
            userMapper.insert(u);
            u.setUid(String.format("%03d", u.getId()));
            userMapper.updateById(u);
        }
    }

    private void seedCategories() {
        String[][] data = {
            {"手机数码", "0"}, {"电脑办公", "0"}, {"服饰鞋包", "0"}, {"食品生鲜", "0"},
            {"家居家装", "0"}, {"美妆护肤", "0"}, {"运动户外", "0"}, {"图书文具", "0"}
        };
        Map<String, Integer> parentIds = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            Category c = new Category();
            c.setName(data[i][0]); c.setParentId(0);
            c.setSortOrder(i + 1);
            categoryMapper.insert(c);
            parentIds.put(data[i][0], c.getId());
        }

        String[][] subData = {
            {"键盘鼠标", "电脑办公"},
            {"女装", "服饰鞋包"}, {"男装", "服饰鞋包"},
            {"休闲零食", "食品生鲜"}, {"生鲜水果", "食品生鲜"},
            {"家具", "家居家装"}, {"灯具", "家居家装"},
            {"面部护理", "美妆护肤"}, {"彩妆", "美妆护肤"},
            {"跑步鞋", "运动户外"}, {"健身器材", "运动户外"}
        };
        for (int i = 0; i < subData.length; i++) {
            Category c = new Category();
            c.setName(subData[i][0]);
            c.setParentId(parentIds.get(subData[i][1]));
            c.setSortOrder(i + 1);
            categoryMapper.insert(c);
        }
    }

    private void seedProducts() {
        String[] names = {
            "iPhone 15 Pro Max 旗舰手机", "Samsung Galaxy S24 Ultra 旗舰手机", "MacBook Pro 16 M3 笔记本电脑",
            "AirPods Pro 2 蓝牙耳机", "Sony WH-1000XM5 头戴式降噪耳机", "Logitech MX Master 3S 无线鼠标",
            "iPad Air M2 平板电脑", "Xiaomi 14 Pro 智能手机", "Dell XPS 15 笔记本电脑", "ThinkPad X1 Carbon 商务笔记本",
            "RGB机械键盘", "27英寸4K显示器", "无线鼠标",
            "7合1 USB-C扩展坞", "女士羊绒大衣", "男士商务西装套装",
            "超轻跑步鞋", "优质纯棉T恤",
            "混合坚果礼盒", "有机绿茶",
            "挪威新鲜三文鱼", "澳洲牛排",
            "北欧落地灯", "北欧简约书桌",
            "记忆棉枕头", "智能LED灯泡",
            "玻尿酸精华液", "维生素C亮肤面霜",
            "哑光唇膏套装", "抗皱眼霜",
            "专业跑步鞋", "优质瑜伽垫",
            "弹力带套装", "20kg哑铃套装",
            "2024年度畅销小说", "Python编程入门",
            "素描艺术套装", "书法练习字帖",
            "便携蓝牙音箱", "20000mAh充电宝",
            "无线充电支架", "手机保护壳",
            "铝合金笔记本支架", "1080p高清摄像头",
            "大号游戏鼠标垫", "桌面收纳套装",
            "电动牙刷", "专业吹风机"
        };

        Integer[] catIds = {1,1,2,1,1,2, 1,1,2,2, 2,2,2,2, 5,5, 10,5, 7,7, 13,13, 5,5, 5,5, 6,6, 9,6, 10,10, 11,12, 12,12, 12,12, 1,1, 1,1, 2,2, 2,2, 3,3};

        for (int i = 0; i < names.length; i++) {
            Product p = new Product();
            p.setName(names[i]);
            p.setDescription("高品质" + names[i]);
            p.setDetail("<p>这是一款高品质的<strong>" + names[i] + "</strong>，精选优质材料，严格品控，给您最好的体验。</p>");
            p.setCategoryId(catIds[i]);
            int priceVal = 99 + rand.nextInt(9901);
            p.setPrice(new BigDecimal(priceVal));
            p.setOriginalPrice(new BigDecimal(priceVal + rand.nextInt(3000)));
            p.setStock(50 + rand.nextInt(950));
            p.setSales(rand.nextInt(5000));
            p.setStatus("on");
            p.setIsNew(i < 10);
            p.setIsHot(i >= 5 && i < 15);
            p.setIsPromotion(i >= 40);
            List<String> images = new ArrayList<>();
            images.add("https://placehold.co/600x600?text=" + names[i].replace(" ", "+"));
            p.setImages(images);

            // Specs
            List<Map<String, Object>> specs = new ArrayList<>();
            Map<String, Object> colorSpec = new HashMap<>();
            colorSpec.put("name", "颜色");
            colorSpec.put("values", Arrays.asList("黑色", "白色", "蓝色"));
            specs.add(colorSpec);
            Map<String, Object> sizeSpec = new HashMap<>();
            sizeSpec.put("name", "规格");
            sizeSpec.put("values", Arrays.asList("标准版", "Pro版"));
            specs.add(sizeSpec);
            p.setSpecs(specs);

            productMapper.insert(p);
        }
    }

    private void seedBanners() {
        String[][] data = {
            {"618年中大促", "https://placehold.co/1200x400?text=618+Sale", "/products"},
            {"iPhone 15 Pro", "https://placehold.co/1200x400?text=iPhone+15+Pro", "/products/1"},
            {"春季焕新", "https://placehold.co/1200x400?text=Spring+New", "/products"}
        };
        for (int i = 0; i < data.length; i++) {
            Banner b = new Banner();
            b.setTitle(data[i][0]); b.setImageUrl(data[i][1]);
            b.setLinkUrl(data[i][2]); b.setSortOrder(i + 1); b.setStatus("on");
            bannerMapper.insert(b);
        }
    }

    private void seedAnnouncements() {
        String[][] data = {
            {"618大促活动通知", "618年中大促即将开始，全场商品低至5折，敬请期待！"},
            {"新用户专享优惠券", "新注册用户可领取50元优惠券，满200元即可使用。"},
            {"春节物流安排", "春节期间物流正常发货，部分偏远地区可能延迟1-2天。"}
        };
        for (String[] d : data) {
            Announcement a = new Announcement();
            a.setTitle(d[0]); a.setContent(d[1]); a.setStatus("on");
            announcementMapper.insert(a);
        }
    }

    private void seedCoupons() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        Object[][] data = {
            {"新人50元券", "fixed", new BigDecimal(50), new BigDecimal(200), now, addDays(30), 1000},
            {"满300减30", "fixed", new BigDecimal(30), new BigDecimal(300), now, addDays(15), 500},
            {"满500减60", "fixed", new BigDecimal(60), new BigDecimal(500), now, addDays(7), 300},
            {"全场9折券", "percent", new BigDecimal(10), new BigDecimal(100), now, addDays(20), 800}
        };
        for (Object[] d : data) {
            Coupon c = new Coupon();
            c.setName((String) d[0]); c.setType((String) d[1]);
            c.setValue((BigDecimal) d[2]); c.setMinAmount((BigDecimal) d[3]);
            c.setStartTime((Date) d[4]); c.setEndTime((Date) d[5]);
            c.setTotal((Integer) d[6]); c.setUsed(0); c.setStatus("active");
            couponMapper.insert(c);
        }
    }

    private Date addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    private void seedAddresses() {
        String[][] data = {
            {"2", "张三", "13800000001", "北京市", "北京市", "朝阳区", "建国路100号"},
            {"2", "张三", "13800000001", "上海市", "上海市", "浦东新区", "陆家嘴金融街88号"},
            {"3", "李四", "13800000002", "广东省", "广州市", "天河区", "体育西路200号"},
            {"4", "王五", "13800000003", "浙江省", "杭州市", "西湖区", "文三路300号"},
            {"5", "赵六", "13800000004", "四川省", "成都市", "武侯区", "天府大道400号"},
            {"6", "钱七", "13800000005", "江苏省", "南京市", "鼓楼区", "中山路500号"}
        };
        for (String[] d : data) {
            Address a = new Address();
            a.setUserId(Integer.parseInt(d[0])); a.setReceiver(d[1]);
            a.setPhone(d[2]); a.setProvince(d[3]); a.setCity(d[4]);
            a.setDistrict(d[5]); a.setDetail(d[6]);
            a.setIsDefault(d[0].equals("2") && d[4].equals("朝阳区"));
            addressMapper.insert(a);
        }
    }

    private void seedOrders() {
        String[] statuses = {"pending_payment", "pending_payment", "pending_shipment", "pending_shipment",
                "shipped", "shipped", "shipped", "completed", "completed", "completed",
                "cancelled", "pending_payment", "pending_shipment", "shipped", "completed",
                "pending_payment", "pending_shipment", "shipped", "completed", "cancelled",
                "pending_payment", "pending_payment", "shipped", "completed", "completed",
                "pending_shipment", "shipped", "completed", "pending_payment", "completed"};

        for (int i = 0; i < 30; i++) {
            int itemCount = i < 15 ? 1 : 2;
            BigDecimal total = BigDecimal.ZERO;

            // Get random products
            List<Product> allProducts = productMapper.selectList(null);
            List<Product> orderProducts = new ArrayList<>();
            for (int j = 0; j < itemCount; j++) {
                orderProducts.add(allProducts.get(rand.nextInt(allProducts.size())));
            }

            // Calculate total
            for (Product p : orderProducts) {
                total = total.add(p.getPrice().multiply(new BigDecimal(rand.nextInt(3) + 1)));
            }
            BigDecimal payment = total.subtract(new BigDecimal(rand.nextInt(30)));

            Order order = new Order();
            order.setOrderNo(OrderNoUtil.generateOrderNo());
            order.setUserId(2 + rand.nextInt(5)); // random user 2-6

            Map<String, Object> addr = new HashMap<>();
            addr.put("receiver", "收货人"); addr.put("phone", "13800000000");
            addr.put("province", "北京市"); addr.put("city", "北京市");
            addr.put("district", "朝阳区"); addr.put("detail", "测试地址");
            order.setAddressSnapshot(addr);

            order.setTotalAmount(total);
            order.setDiscountAmount(new BigDecimal(0));
            order.setPaymentAmount(payment.compareTo(BigDecimal.ZERO) > 0 ? payment : total);
            order.setStatus(statuses[i]);
            order.setPaymentMethod("alipay");

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -(30 - i));
            order.setCreatedAt(cal.getTime());

            orderMapper.insert(order);

            // Order items
            for (int j = 0; j < orderProducts.size(); j++) {
                Product p = orderProducts.get(j);
                int qty = rand.nextInt(3) + 1;
                OrderItem oi = new OrderItem();
                oi.setOrderId(order.getId());
                oi.setProductId(p.getId());
                oi.setPrice(p.getPrice());
                oi.setQuantity(qty);

                Map<String, Object> snap = new HashMap<>();
                snap.put("name", p.getName());
                snap.put("images", p.getImages());
                oi.setProductSnapshot(snap);
                orderItemMapper.insert(oi);
            }
        }
    }

    private void seedReviews() {
        List<User> users = userMapper.selectList(null);
        List<Product> products = productMapper.selectList(null);
        String[] comments = {
            "很好用，质量不错！", "性价比很高，推荐购买。", "物流很快，包装完好。",
            "使用了一段时间，感觉很满意。", "颜色和图片一致，非常好看。"
        };

        for (int i = 0; i < 50; i++) {
            Review r = new Review();
            r.setUserId(users.get(rand.nextInt(users.size())).getId());
            r.setProductId(products.get(rand.nextInt(products.size())).getId());
            r.setRating(3 + rand.nextInt(3));
            r.setContent(comments[rand.nextInt(comments.length)]);
            reviewMapper.insert(r);
        }
    }

    private void seedFavorites() {
        List<Product> products = productMapper.selectList(null);
        for (int i = 0; i < 30; i++) {
            Favorite f = new Favorite();
            f.setUserId(2 + rand.nextInt(5));
            f.setProductId(products.get(rand.nextInt(products.size())).getId());
            try { favoriteMapper.insert(f); } catch (Exception ignored) {}
        }
    }

    private void seedFeedbacks() {
        String[] contents = {"希望能增加更多支付方式", "APP什么时候上线？", "商品质量很好，会继续支持"};
        for (int i = 0; i < 3; i++) {
            Feedback fb = new Feedback();
            fb.setUserId(2 + i);
            fb.setContent(contents[i]);
            fb.setStatus("pending");
            feedbackMapper.insert(fb);
        }
    }

    private void seedCartItems() {
        List<Product> products = productMapper.selectList(null);
        for (int i = 0; i < 10; i++) {
            CartItem ci = new CartItem();
            ci.setUserId(2 + rand.nextInt(3));
            ci.setProductId(products.get(rand.nextInt(products.size())).getId());
            ci.setQuantity(1 + rand.nextInt(3));
            ci.setSelected(true);
            try { cartItemMapper.insert(ci); } catch (Exception ignored) {}
        }
    }

    private void seedFlashSales() {
        List<Product> products = productMapper.selectList(null);
        // Pick 6 random products for flash sales
        List<Product> selected = new ArrayList<>(products);
        Collections.shuffle(selected, rand);
        selected = selected.subList(0, Math.min(6, selected.size()));

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.HOUR, 2); // end in 2 hours
        Date end2h = cal.getTime();
        cal.add(Calendar.HOUR, 22); // end in 24 hours
        Date end24h = cal.getTime();

        for (int i = 0; i < selected.size(); i++) {
            Product p = selected.get(i);
            FlashSale fs = new FlashSale();
            fs.setProductId(p.getId());
            // Flash price: 50%-80% of original price
            BigDecimal discount = new BigDecimal(0.5 + rand.nextDouble() * 0.3);
            fs.setFlashPrice(p.getPrice().multiply(discount).setScale(2, java.math.RoundingMode.HALF_UP));
            fs.setStock(20 + rand.nextInt(80));
            fs.setSold(rand.nextInt(15));
            fs.setStartTime(now);
            // First 3 end in 2 hours, rest end in 24 hours
            fs.setEndTime(i < 3 ? end2h : end24h);
            fs.setStatus("active");
            flashSaleMapper.insert(fs);
        }
    }
}
