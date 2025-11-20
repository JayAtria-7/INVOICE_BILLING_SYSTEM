/**
 * Module descriptor for Invoice Billing System
 */
module InvoiceBillingSystem {
	requires java.sql;       // For JDBC database connectivity
    requires java.desktop;   // For Swing GUI components
    
    // Note: For ViewInvoicesServlet to work, you need to add servlet-api.jar to your classpath
    // Download from: https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api
    // Or comment out the requires below if not using servlets
    // requires java.servlet;  // Uncomment when servlet-api is available
}
