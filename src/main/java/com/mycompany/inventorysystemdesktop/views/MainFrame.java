package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.User;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    private final User currentUser;
    private JTabbedPane tabbedPane;
    
    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        initComponents();
        setupFrame();
    }
    
    private void initComponents() {
        // إعداد الإطار الرئيسي
        setTitle("نظام إدارة المخزون");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // إنشاء شريط التبويب
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // إضافة التبويبات
        addTabs();
        
        // إضافة شريط التبويب إلى الإطار
        add(createHeader(), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void addTabs() {
        // التبويب الرئيسي (لوحة المعلومات)
        tabbedPane.addTab("لوحة المعلومات", new DashboardPanel(currentUser));
        
        // تبويب المواد
        tabbedPane.addTab("المواد", new MaterialPanel(currentUser));
        
        // تبويب التصنيفات
        tabbedPane.addTab("التصنيفات", new CategoryPanel(currentUser));
        
        // تبويب الوارد
        tabbedPane.addTab("الوارد", new TransactionsPanel(currentUser, "IN"));
        
        // تبويب المنصرف
        tabbedPane.addTab("المنصرف", new TransactionsPanel(currentUser, "OUT"));
        
        // تبويب التقارير
        tabbedPane.addTab("التقارير", new ReportPanel());
        
        // إذا كان المستخدم مدير، أضف تبويب المستخدمين
        if (currentUser.getRole().equals("ADMIN")) {
            tabbedPane.addTab("المستخدمين", new UsersPanel(currentUser));
        }
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // معلومات المستخدم
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel welcomeLabel = new JLabel("مرحباً، " + currentUser.getFullName());
        JButton logoutButton = new JButton("تسجيل الخروج");
        
        logoutButton.addActionListener(e -> logout());
        
        userInfo.add(logoutButton);
        userInfo.add(welcomeLabel);
        
        // إضافة التاريخ والوقت (يمكن إضافة ساعة حية لاحقاً)
        JLabel dateTimeLabel = new JLabel(new java.util.Date().toString());
        
        headerPanel.add(userInfo, BorderLayout.EAST);
        headerPanel.add(dateTimeLabel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private void setupFrame() {
        // تعيين الحجم والموقع
        setSize(1200, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // توسيط النافذة
        
        // تعيين أيقونة التطبيق
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app_icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("لم يتم العثور على أيقونة التطبيق");
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "هل أنت متأكد من تسجيل الخروج؟",
            "تأكيد تسجيل الخروج",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // إغلاق النافذة الحالية
            new LoginDialog(null).setVisible(true);
        }
    }
}