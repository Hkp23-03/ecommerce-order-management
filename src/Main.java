
import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";

    static final String USER = "root";
    static final String PASSWORD = "Harshi2303$";

    static Scanner sc = new Scanner(System.in);

    // Establish database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // (a) Insert at least three order records
    public static void insertOrders() {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter Order " + i + " Details:");

                System.out.print("Order ID: ");
                int id = Integer.parseInt(sc.nextLine());

                System.out.print("Customer Name: ");
                String name = sc.nextLine();

                System.out.print("Product Name: ");
                String product = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = Integer.parseInt(sc.nextLine());

                System.out.print("Unit Price: ");
                double price = Double.parseDouble(sc.nextLine());

                System.out.print("Status: ");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, product);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted successfully!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID! Record not inserted.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numeric values.");
        }
    }

    // (b) Display orders by status and quantity > 2
    // Sorted by unit price in descending order
    public static void displayOrders() {

        String sql = "SELECT * FROM Orders " +
                "WHERE status = ? AND quantity > 2 " +
                "ORDER BY unit_price DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Order Status: ");
            String status = sc.nextLine();

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            System.out.println("\nOrders Found:");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println(
                        "Order ID: " + rs.getInt("order_id") +
                                " | Customer: " + rs.getString("customer_name") +
                                " | Product: " + rs.getString("product_name") +
                                " | Quantity: " + rs.getInt("quantity") +
                                " | Price: " + rs.getDouble("unit_price") +
                                " | Status: " + rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("No matching orders found.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // (c) Increase quantity of an existing order
    public static void increaseQuantity() {

        String sql = "UPDATE Orders SET quantity = quantity + ? " +
                "WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Order ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter Additional Quantity: ");
            int additional = Integer.parseInt(sc.nextLine());

            ps.setInt(1, additional);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Order quantity updated successfully!");
            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Enter valid numeric values.");
        }
    }

    // (d) Update order status only if order exists
    // and new status is valid
    public static void updateStatus() {

        String select = "SELECT * FROM Orders WHERE order_id = ?";

        String update = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(select)) {

            System.out.print("Enter Order ID: ");
            int id = Integer.parseInt(sc.nextLine());

            // Check whether order exists
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("\nExisting Order Details:");

                System.out.println(
                        "Order ID: " + rs.getInt("order_id") +
                                " | Customer: " + rs.getString("customer_name") +
                                " | Product: " + rs.getString("product_name") +
                                " | Quantity: " + rs.getInt("quantity") +
                                " | Price: " + rs.getDouble("unit_price") +
                                " | Status: " + rs.getString("status")
                );

                System.out.print("\nEnter New Status: ");
                String status = sc.nextLine();

                // Validate status
                if (!(status.equalsIgnoreCase("Pending") ||
                        status.equalsIgnoreCase("Shipped") ||
                        status.equalsIgnoreCase("Delivered"))) {

                    System.out.println("Invalid Status!");
                    System.out.println(
                            "Allowed: Pending, Shipped, Delivered");
                    return;
                }

                // Normalize status
                status = status.substring(0, 1).toUpperCase()
                        + status.substring(1).toLowerCase();

                try (PreparedStatement ups =
                             con.prepareStatement(update)) {

                    ups.setString(1, status);
                    ups.setInt(2, id);

                    ups.executeUpdate();
                }

                // Display updated order details
                try (PreparedStatement check =
                             con.prepareStatement(select)) {

                    check.setInt(1, id);

                    ResultSet updated = check.executeQuery();

                    if (updated.next()) {
                        System.out.println("\nUpdated Order Details:");

                        System.out.println(
                                "Order ID: " + updated.getInt("order_id") +
                                        " | Customer: " + updated.getString("customer_name") +
                                        " | Product: " + updated.getString("product_name") +
                                        " | Quantity: " + updated.getInt("quantity") +
                                        " | Price: " + updated.getDouble("unit_price") +
                                        " | Status: " + updated.getString("status")
                        );
                    }
                }

            } else {
                System.out.println("Order ID does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid Order ID.");
        }
    }

    // (e) Display total order value and delete
    // only if status is Cancelled
    public static void totalValueAndDelete() {

        String sum = "SELECT SUM(quantity * unit_price) AS total " +
                "FROM Orders";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sum);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("\nTotal Order Value: " +
                        rs.getDouble("total"));
            }

            System.out.print("\nEnter Order ID to delete: ");
            int id = Integer.parseInt(sc.nextLine());

            String delete = "DELETE FROM Orders " +
                    "WHERE order_id = ? " +
                    "AND status = 'Cancelled'";

            try (PreparedStatement ds = con.prepareStatement(delete)) {

                ds.setInt(1, id);

                int rows = ds.executeUpdate();

                if (rows > 0) {
                    System.out.println(
                            "Cancelled order deleted successfully!");
                } else {
                    System.out.println(
                            "Order cannot be deleted. " +
                                    "It does not exist or its status is not Cancelled.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid Order ID.");
        }
    }

    // Main menu
    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            return;
        }

        while (true) {

            System.out.println("\n===== E-COMMERCE ORDER MANAGEMENT SYSTEM =====");

            System.out.println("1. Insert Three Orders");
            System.out.println("2. Display Orders by Status");
            System.out.println("3. Increase Order Quantity");
            System.out.println("4. Update Order Status");
            System.out.println("5. Display Total Value and Delete Cancelled Order");
            System.out.println("6. Exit");

            System.out.print("\nEnter Your Choice: ");

            String input = sc.nextLine();

            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice! Enter a number.");
                continue;
            }

            switch (choice) {

                case 1:
                    insertOrders();
                    break;

                case 2:
                    displayOrders();
                    break;

                case 3:
                    increaseQuantity();
                    break;

                case 4:
                    updateStatus();
                    break;

                case 5:
                    totalValueAndDelete();
                    break;

                case 6:
                    System.out.println("Thank you for using the system!");
                    return;

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }
}
