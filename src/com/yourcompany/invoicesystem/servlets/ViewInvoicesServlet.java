/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.servlets;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servlet for viewing invoices in a web interface.
 * Supports viewing all invoices and detailed view of individual invoices.
 */
@WebServlet(name = "ViewInvoicesServlet", urlPatterns = {"/invoices", "/invoices/*"})
public class ViewInvoicesServlet extends HttpServlet {
    
    private InvoiceDAO invoiceDAO;
    private InvoiceItemDAO invoiceItemDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    @Override
    public void init() throws ServletException {
        super.init();
        invoiceDAO = new InvoiceDAO();
        invoiceItemDAO = new InvoiceItemDAO();
    }
    
    /**
     * Handles GET requests for viewing invoices.
     * URL patterns:
     * - /invoices - Lists all invoices
     * - /invoices/{id} - Shows details of specific invoice
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Show all invoices
                showAllInvoices(out);
            } else {
                // Show specific invoice details
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length > 1) {
                    int invoiceId = Integer.parseInt(pathParts[1]);
                    showInvoiceDetails(out, invoiceId);
                } else {
                    showError(out, "Invalid invoice ID");
                }
            }
        } catch (NumberFormatException e) {
            showError(out, "Invalid invoice ID format");
        } catch (Exception e) {
            showError(out, "Error loading invoice data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays a list of all invoices.
     */
    private void showAllInvoices(PrintWriter out) {
        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>All Invoices - ProBilling</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println("h1 { color: #3c46c8; }");
        out.println("table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("th { background-color: #3c46c8; color: white; padding: 12px; text-align: left; }");
        out.println("td { padding: 10px; border-bottom: 1px solid #ddd; }");
        out.println("tr:hover { background-color: #f8f9ff; }");
        out.println("a { color: #3c46c8; text-decoration: none; font-weight: bold; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println(".amount { text-align: right; font-weight: bold; color: #198754; }");
        out.println(".container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }");
        out.println(".no-data { text-align: center; padding: 40px; color: #666; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Invoice List</h1>");
        
        if (invoices == null || invoices.isEmpty()) {
            out.println("<div class='no-data'>No invoices found.</div>");
        } else {
            out.println("<table>");
            out.println("<thead>");
            out.println("<tr><th>Invoice ID</th><th>Date</th><th>Total Amount</th><th>Action</th></tr>");
            out.println("</thead>");
            out.println("<tbody>");
            
            for (Invoice invoice : invoices) {
                out.println("<tr>");
                out.println("<td>#" + invoice.getInvoiceID() + "</td>");
                out.println("<td>" + invoice.getInvoiceDate().format(DATE_FORMATTER) + "</td>");
                out.println("<td class='amount'>€ " + String.format("%.2f", invoice.getTotalAmount()) + "</td>");
                out.println("<td><a href='invoices/" + invoice.getInvoiceID() + "'>View Details</a></td>");
                out.println("</tr>");
            }
            
            out.println("</tbody>");
            out.println("</table>");
            out.println("<p style='margin-top: 20px; color: #666;'>Total Invoices: " + invoices.size() + "</p>");
        }
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * Displays details of a specific invoice including all line items.
     */
    private void showInvoiceDetails(PrintWriter out, int invoiceId) {
        Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
        
        if (invoice == null) {
            showError(out, "Invoice #" + invoiceId + " not found");
            return;
        }
        
        List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoiceId);
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Invoice #" + invoiceId + " - ProBilling</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #3c46c8; border-bottom: 3px solid #3c46c8; padding-bottom: 10px; }");
        out.println(".info { margin: 20px 0; }");
        out.println(".info-label { font-weight: bold; color: #666; }");
        out.println("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        out.println("th { background-color: #f8f9fa; padding: 12px; text-align: left; border-bottom: 2px solid #dee2e6; }");
        out.println("td { padding: 10px; border-bottom: 1px solid #dee2e6; }");
        out.println(".amount { text-align: right; font-weight: bold; }");
        out.println(".total-row { background-color: #f8f9ff; font-weight: bold; font-size: 1.1em; }");
        out.println(".total-row td { border-top: 2px solid #3c46c8; color: #198754; }");
        out.println(".back-link { display: inline-block; margin-top: 20px; color: #3c46c8; text-decoration: none; }");
        out.println(".back-link:hover { text-decoration: underline; }");
        out.println(".no-items { text-align: center; padding: 20px; color: #666; font-style: italic; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Invoice #" + invoice.getInvoiceID() + "</h1>");
        
        out.println("<div class='info'>");
        out.println("<p><span class='info-label'>Date:</span> " + invoice.getInvoiceDate().format(DATE_FORMATTER) + "</p>");
        out.println("<p><span class='info-label'>Total Amount:</span> <span style='color: #198754; font-size: 1.2em; font-weight: bold;'>€ " + 
                   String.format("%.2f", invoice.getTotalAmount()) + "</span></p>");
        out.println("</div>");
        
        out.println("<h2>Invoice Items</h2>");
        
        if (items == null || items.isEmpty()) {
            out.println("<div class='no-items'>No items found for this invoice.</div>");
        } else {
            out.println("<table>");
            out.println("<thead>");
            out.println("<tr><th>Item ID</th><th>Product ID</th><th>Quantity</th><th>Unit Price</th><th>Total</th></tr>");
            out.println("</thead>");
            out.println("<tbody>");
            
            for (InvoiceItem item : items) {
                java.math.BigDecimal itemTotal = item.getPriceAtSale().multiply(new java.math.BigDecimal(item.getQuantity()));
                out.println("<tr>");
                out.println("<td>" + item.getInvoiceItemID() + "</td>");
                out.println("<td>" + item.getProductID() + "</td>");
                out.println("<td>" + item.getQuantity() + "</td>");
                out.println("<td class='amount'>€ " + String.format("%.2f", item.getPriceAtSale()) + "</td>");
                out.println("<td class='amount'>€ " + String.format("%.2f", itemTotal) + "</td>");
                out.println("</tr>");
            }
            
            out.println("<tr class='total-row'>");
            out.println("<td colspan='4' style='text-align: right;'>TOTAL:</td>");
            out.println("<td class='amount'>€ " + String.format("%.2f", invoice.getTotalAmount()) + "</td>");
            out.println("</tr>");
            
            out.println("</tbody>");
            out.println("</table>");
        }
        
        out.println("<a href='" + getServletContext().getContextPath() + "/invoices' class='back-link'>← Back to Invoice List</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * Displays an error message.
     */
    private void showError(PrintWriter out, String message) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Error - ProBilling</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 600px; margin: 100px auto; background: white; padding: 40px; border-radius: 8px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #dc3545; }");
        out.println("p { color: #666; font-size: 1.1em; }");
        out.println("a { color: #3c46c8; text-decoration: none; font-weight: bold; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Error</h1>");
        out.println("<p>" + message + "</p>");
        out.println("<p><a href='" + getServletContext().getContextPath() + "/invoices'>← Back to Invoice List</a></p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}

