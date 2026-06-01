package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;

    public Map<String, Object> dashboard() {
        Map<String, Object> result = new HashMap<>();

        Integer userCount = Math.toIntExact(userMapper.selectCount(null));
        Integer orderCount = Math.toIntExact(orderMapper.selectCount(null));

        // Total sales (excluding cancelled)
        QueryWrapper<Order> salesQw = new QueryWrapper<Order>().ne("status", "cancelled");
        List<Order> validOrders = orderMapper.selectList(salesQw);
        BigDecimal totalSales = BigDecimal.ZERO;
        for (Order o : validOrders) {
            totalSales = totalSales.add(o.getPaymentAmount());
        }

        // Today's orders and sales
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date today = cal.getTime();

        QueryWrapper<Order> todayQw = new QueryWrapper<Order>().ge("created_at", today);
        Integer todayOrders = Math.toIntExact(orderMapper.selectCount(todayQw));

        BigDecimal todaySales = BigDecimal.ZERO;
        List<Order> todayOrdersList = orderMapper.selectList(todayQw);
        for (Order o : todayOrdersList) {
            if (!"cancelled".equals(o.getStatus())) {
                todaySales = todaySales.add(o.getPaymentAmount());
            }
        }

        result.put("userCount", userCount);
        result.put("orderCount", orderCount);
        result.put("totalSales", totalSales);
        result.put("todayOrders", todayOrders);
        result.put("todaySales", todaySales);

        // Order status distribution
        List<Map<String, Object>> orderStatus = new ArrayList<>();
        String[] statuses = {"pending_payment", "pending_shipment", "shipped", "completed", "cancelled", "refunding"};
        String[] statusNames = {"待支付", "待发货", "已发货", "已完成", "已取消", "退款中"};
        for (int i = 0; i < statuses.length; i++) {
            Integer count = Math.toIntExact(orderMapper.selectCount(
                    new QueryWrapper<Order>().eq("status", statuses[i])));
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", count);
            orderStatus.add(item);
        }
        result.put("orderStatus", orderStatus);

        // Top 10 products by sales
        QueryWrapper<Product> topQw = new QueryWrapper<Product>()
                .orderByDesc("sales").last("LIMIT 10");
        List<Product> topProducts = productMapper.selectList(topQw);
        List<Map<String, Object>> topList = new ArrayList<>();
        for (Product p : topProducts) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", p.getName());
            item.put("sales", p.getSales());
            topList.add(item);
        }
        result.put("topProducts", topList);

        // Last 7 days daily sales
        List<Map<String, Object>> dailySales = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -i);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date dayStart = c.getTime();
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            Date dayEnd = c.getTime();

            QueryWrapper<Order> dayQw = new QueryWrapper<Order>()
                    .between("created_at", dayStart, dayEnd)
                    .ne("status", "cancelled");
            List<Order> dayOrders = orderMapper.selectList(dayQw);
            BigDecimal dayAmount = BigDecimal.ZERO;
            for (Order o : dayOrders) {
                dayAmount = dayAmount.add(o.getPaymentAmount());
            }

            Map<String, Object> item = new HashMap<>();
            item.put("date", new SimpleDateFormat("MM-dd").format(dayStart));
            item.put("amount", dayAmount);
            dailySales.add(item);
        }
        result.put("dailySales", dailySales);

        return result;
    }

    public byte[] exportOrders() throws Exception {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().orderByDesc("created_at"));

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("订单数据");
        Row header = sheet.createRow(0);
        String[] cols = {"订单号", "订单金额", "实付金额", "状态", "商品数", "创建时间"};
        for (int i = 0; i < cols.length; i++) {
            header.createCell(i).setCellValue(cols[i]);
        }

        int rowIdx = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Order o : orders) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(o.getOrderNo());
            row.createCell(1).setCellValue(o.getTotalAmount().doubleValue());
            row.createCell(2).setCellValue(o.getPaymentAmount().doubleValue());
            row.createCell(3).setCellValue(o.getStatus());
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
            row.createCell(4).setCellValue(items.size());
            row.createCell(5).setCellValue(o.getCreatedAt() != null ? sdf.format(o.getCreatedAt()) : "");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return bos.toByteArray();
    }

    public Map<String, Object> importProducts(MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        int fail = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Product p = new Product();
                    p.setName(getCellString(row, 0));
                    if (p.getName() == null || p.getName().isEmpty()) {
                        fail++; errors.add("第" + (i + 1) + "行：商品名称不能为空");
                        continue;
                    }
                    p.setDescription(getCellString(row, 1));
                    p.setDetail(getCellString(row, 2));
                    String catId = getCellString(row, 3);
                    if (catId != null && !catId.isEmpty()) p.setCategoryId(Integer.parseInt(catId));
                    String price = getCellString(row, 4);
                    if (price != null && !price.isEmpty()) p.setPrice(new BigDecimal(price));
                    String origPrice = getCellString(row, 5);
                    if (origPrice != null && !origPrice.isEmpty()) p.setOriginalPrice(new BigDecimal(origPrice));
                    String stock = getCellString(row, 6);
                    if (stock != null && !stock.isEmpty()) p.setStock(Integer.parseInt(stock));
                    else p.setStock(0);
                    String tags = getCellString(row, 7);
                    if (tags != null && !tags.isEmpty()) {
                        p.setTags(Arrays.asList(tags.split("[,，]")));
                    }
                    String status = getCellString(row, 8);
                    p.setStatus(status != null && !status.isEmpty() ? status : "on");
                    String isNew = getCellString(row, 9);
                    p.setIsNew("是".equals(isNew) || "1".equals(isNew) || "true".equalsIgnoreCase(isNew));
                    String isHot = getCellString(row, 10);
                    p.setIsHot("是".equals(isHot) || "1".equals(isHot) || "true".equalsIgnoreCase(isHot));
                    String isPromo = getCellString(row, 11);
                    p.setIsPromotion("是".equals(isPromo) || "1".equals(isPromo) || "true".equalsIgnoreCase(isPromo));

                    p.setSales(0);
                    p.setCreatedAt(new Date());
                    p.setUpdatedAt(new Date());
                    productMapper.insert(p);
                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
        }
        result.put("success", success);
        result.put("fail", fail);
        result.put("errors", errors);
        return result;
    }

    public byte[] exportProductTemplate() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("商品导入模板");
        Row header = sheet.createRow(0);
        String[] cols = {"商品名称*", "描述", "详情", "分类ID", "售价*", "原价", "库存*", "标签(逗号分隔)", "状态(on/off)", "新品(是/否)", "热销(是/否)", "促销(是/否)"};
        Row styleRow = sheet.createRow(1);
        String[] example = {"示例商品", "这是一个示例", "<p>详情HTML</p>", "1", "99.00", "199.00", "100", "热卖,新品", "on", "是", "否", "否"};
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            styleRow.createCell(i).setCellValue(example[i]);
        }
        sheet.autoSizeColumn(0);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return bos.toByteArray();
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String val = cell.getStringCellValue();
        return val != null ? val.trim() : null;
    }
}
