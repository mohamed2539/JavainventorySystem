package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.*;
import com.mycompany.inventorysystemdesktop.dao.*;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;

public class TransactionPrintDialog extends JDialog implements Printable {
    private final Transaction transaction;
    private final MaterialDAO materialDAO;
    private final UserDAO userDAO;
    
    public TransactionPrintDialog(Window owner, Transaction transaction) {
        super(owner, "طباعة المعاملة", ModalityType.APPLICATION_MODAL);
        this.transaction = transaction;
        this.materialDAO = new MaterialDAO();
        this.userDAO = new UserDAO();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton printButton = new JButton("طباعة");
        JButton closeButton = new JButton("إغلاق");
        
        printButton.addActionListener(e -> print());
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        // إضافة معاينة الطباعة
        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                printContent((Graphics2D) g, getWidth(), getHeight());
            }
        };
        previewPanel.setPreferredSize(new Dimension(600, 800));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        add(new JScrollPane(previewPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this,
                    "خطأ في الطباعة: " + e.getMessage(),
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        printContent(g2d, (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight());
        
        return PAGE_EXISTS;
    }
    
    private void printContent(Graphics2D g2d, int width, int height) {
        try {
            Material material = materialDAO.findById(transaction.getMaterialId());
            User user = userDAO.findById(transaction.getUserId());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("سند " + (transaction.getType().equals("IN") ? "استلام" : "صرف"), width/2 - 50, 30);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            int y = 80;
            int leftMargin = 50;
            
            g2d.drawString("رقم المعاملة: " + transaction.getId(), leftMargin, y); y += 25;
            g2d.drawString("التاريخ: " + dateFormat.format(transaction.getTransactionDate()), leftMargin, y); y += 25;
            g2d.drawString("رقم المرجع: " + transaction.getReferenceNo(), leftMargin, y); y += 25;
            g2d.drawString("المادة: " + material.getName(), leftMargin, y); y += 25;
            g2d.drawString("الكمية: " + transaction.getQuantity(), leftMargin, y); y += 25;
            g2d.drawString("سعر الوحدة: " + transaction.getUnitPrice(), leftMargin, y); y += 25;
            g2d.drawString("الإجمالي: " + (transaction.getQuantity() * transaction.getUnitPrice()), leftMargin, y); y += 25;
            g2d.drawString("المستخدم: " + user.getFullName(), leftMargin, y); y += 25;
            
            if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
                g2d.drawString("ملاحظات:", leftMargin, y); y += 25;
                g2d.drawString(transaction.getNotes(), leftMargin + 20, y);
            }
            
            // إضافة مكان للتوقيع
            y = height - 100;
            g2d.drawString("توقيع المستلم: ________________", leftMargin, y);
            g2d.drawString("توقيع المسلم: ________________", width - 250, y);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}