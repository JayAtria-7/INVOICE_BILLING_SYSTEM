package com.yourcompany.invoicesystem.service;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.exception.BusinessLogicException;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.model.Product;
import com.yourcompany.invoicesystem.util.SessionManager;
import com.yourcompany.invoicesystem.validation.ValidationException;
import com.yourcompany.invoicesystem.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for Invoice business logic
 * Handles validation and business rules for invoice operations
 */
public class InvoiceService {
    
    private final InvoiceDAO invoiceDAO;
    private final InvoiceItemDAO invoiceItemDAO;
    private final ProductDAO productDAO;
    
    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.invoiceItemDAO = new InvoiceItemDAO();
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Create a new invoice with items
     * @param items List of invoice items
     * @param discountPercentage Discount percentage
     * @param taxAmount Tax amount
     * @return Created invoice
     * @throws BusinessLogicException if creation fails
     */
    public Invoice createInvoice(List<InvoiceItem> items, BigDecimal discountPercentage, BigDecimal taxAmount) 
            throws BusinessLogicException {
        try {
            // Validate inputs
            if (items == null || items.isEmpty()) {
                throw new BusinessLogicException("Invoice must contain at least one item");
            }
            
            Validator.validatePercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
            Validator.validateAmount(taxAmount != null ? taxAmount : BigDecimal.ZERO);
            
            // Validate stock availability for all items
            for (InvoiceItem item : items) {
                Product product = productDAO.getProductById(item.getProductID());
                if (product == null) {
                    throw new BusinessLogicException("Product not found with ID: " + item.getProductID());
                }
                
                if (product.getStock() < item.getQuantity()) {
                    throw new BusinessLogicException(
                        "Insufficient stock for product: " + product.getName() + 
                        ". Available: " + product.getStock() + ", Requested: " + item.getQuantity());
                }
            }
            
            // Calculate total
            BigDecimal subtotal = BigDecimal.ZERO;
            for (InvoiceItem item : items) {
                BigDecimal itemTotal = item.getPriceAtSale()
                    .multiply(new BigDecimal(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);
            }
            
            // Apply discount
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = subtotal.multiply(discountPercentage)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            BigDecimal totalAmount = subtotal.subtract(discountAmount);
            if (taxAmount != null) {
                totalAmount = totalAmount.add(taxAmount);
            }
            
            // Create invoice
            Invoice invoice = new Invoice();
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setTotalAmount(totalAmount);
            invoice.setDiscountPercentage(discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
            invoice.setTaxAmount(taxAmount != null ? taxAmount : BigDecimal.ZERO);
            invoice.setPaymentStatus("PENDING");
            invoice.setUserID(SessionManager.getCurrentUser().getUserID());
            
            // Save invoice
            int invoiceId = invoiceDAO.addInvoice(invoice);
            invoice.setInvoiceID(invoiceId);
            
            // Save items and update stock
            for (InvoiceItem item : items) {
                item.setInvoiceID(invoiceId);
                invoiceItemDAO.addInvoiceItem(item);
                
                // Reduce stock
                Product product = productDAO.getProductById(item.getProductID());
                product.setStock(product.getStock() - item.getQuantity());
                productDAO.updateProduct(product);
            }
            
            return invoice;
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to create invoice: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get invoice by ID with items
     * @param invoiceId Invoice ID
     * @return Invoice with items
     * @throws BusinessLogicException if retrieval fails
     */
    public Invoice getInvoiceWithItems(int invoiceId) throws BusinessLogicException {
        try {
            Validator.validateId(invoiceId, "Invoice ID");
            
            Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
            if (invoice == null) {
                throw new BusinessLogicException("Invoice not found with ID: " + invoiceId);
            }
            
            return invoice;
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to retrieve invoice: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all invoices
     * @return List of all invoices
     * @throws BusinessLogicException if retrieval fails
     */
    public List<Invoice> getAllInvoices() throws BusinessLogicException {
        try {
            return invoiceDAO.getAllInvoices();
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to retrieve invoices: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update invoice payment status
     * @param invoiceId Invoice ID
     * @param status Payment status
     * @throws BusinessLogicException if update fails
     */
    public void updatePaymentStatus(int invoiceId, String status) throws BusinessLogicException {
        try {
            Validator.validateId(invoiceId, "Invoice ID");
            Validator.validateNotEmpty(status, "Payment status");
            
            Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
            if (invoice == null) {
                throw new BusinessLogicException("Invoice not found with ID: " + invoiceId);
            }
            
            invoice.setPaymentStatus(status);
            invoiceDAO.updateInvoice(invoice);
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update payment status: " + e.getMessage(), e);
        }
    }
}
