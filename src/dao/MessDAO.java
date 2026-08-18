package dao;

import model.MessItem;
import model.MessOrder;
import model.MessOrderItem;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Mess menu and mess orders.
 * mess_order_items is a junction table resolving the M:N relationship
 * between mess_orders and mess_items.
 */
public class MessDAO {

    public List<MessItem> findAllItems() throws SQLException {
        List<MessItem> list = new ArrayList<>();
        String sql = "SELECT * FROM mess_items ORDER BY category, name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapItem(rs));
        }
        return list;
    }

    public MessItem findItemById(int itemId) throws SQLException {
        String sql = "SELECT * FROM mess_items WHERE item_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapItem(rs);
            }
        }
        return null;
    }

    public List<MessItem> findItemsByCategory(String category) throws SQLException {
        List<MessItem> list = new ArrayList<>();
        String sql = "SELECT * FROM mess_items WHERE category=? ORDER BY name";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapItem(rs));
            }
        }
        return list;
    }

    /** Saves a MessOrder header, then batch-inserts all its line items into the junction table. */
    public int saveOrder(MessOrder order) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        String headerSql = "INSERT INTO mess_orders (allocation_id, order_date) VALUES (?,?)";
        String lineSql = "INSERT INTO mess_order_items (order_id, item_id, quantity, unit_price) VALUES (?,?,?,?)";
        int newOrderId = -1;
        try (PreparedStatement psHeader = conn.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS)) {
            psHeader.setInt(1, order.getAllocationId());
            psHeader.setDate(2, Date.valueOf(order.getOrderDate()));
            psHeader.executeUpdate();
            try (ResultSet keys = psHeader.getGeneratedKeys()) {
                if (keys.next()) newOrderId = keys.getInt(1);
            }

            try (PreparedStatement psLine = conn.prepareStatement(lineSql)) {
                for (MessOrderItem line : order.getOrderItems()) {
                    psLine.setInt(1, newOrderId);
                    psLine.setInt(2, line.getItem().getItemId());
                    psLine.setInt(3, line.getQuantity());
                    psLine.setDouble(4, line.getItem().getPrice());
                    psLine.addBatch();          // batch insert
                }
                psLine.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        return newOrderId;
    }

    /** Reconstructs all mess orders (with line items) for a given allocation. */
    public List<MessOrder> findOrdersByAllocation(int allocationId) throws SQLException {
        List<MessOrder> orders = new ArrayList<>();
        String headerSql = "SELECT * FROM mess_orders WHERE allocation_id=? ORDER BY order_date";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(headerSql)) {
            ps.setInt(1, allocationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MessOrder order = new MessOrder(rs.getInt("order_id"), allocationId,
                            rs.getDate("order_date").toLocalDate());
                    attachLineItems(order);
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    private void attachLineItems(MessOrder order) throws SQLException {
        String sql = "SELECT moi.quantity, mi.* FROM mess_order_items moi " +
                "JOIN mess_items mi ON moi.item_id = mi.item_id WHERE moi.order_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, order.getOrderId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    order.addItem(mapItem(rs), rs.getInt("quantity"));
                }
            }
        }
    }

    /** Top-selling mess items overall (aggregate query). */
    public void printTopSellingItems(int limit) throws SQLException {
        String sql = "SELECT mi.name, SUM(moi.quantity) AS total_qty " +
                "FROM mess_order_items moi JOIN mess_items mi ON moi.item_id = mi.item_id " +
                "GROUP BY mi.name ORDER BY total_qty DESC LIMIT ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  %-22s : %d sold%n", rs.getString("name"), rs.getInt("total_qty"));
                }
            }
        }
    }

    private MessItem mapItem(ResultSet rs) throws SQLException {
        return new MessItem(rs.getInt("item_id"), rs.getString("name"),
                rs.getString("category"), rs.getDouble("price"));
    }
}
