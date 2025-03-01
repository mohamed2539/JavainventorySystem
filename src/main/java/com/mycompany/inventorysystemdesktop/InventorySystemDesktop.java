package com.mycompany.inventorysystemdesktop;




import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import com.mycompany.inventorysystemdesktop.views.MainFrame;
import com.mycompany.inventorysystemdesktop.utils.DatabaseConnection;
import com.mycompany.inventorysystemdesktop.views.LoginDialog;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.swing.FontIcon;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Mohammed
 */
public class InventorySystemDesktop {

    public static void main(String[] args) {
        // تعيين نمط الواجهة
        try {
            // تعيين نمط Material Design
             FlatMaterialLighterIJTheme.setup();
            
            // تعيين اتجاه النص من اليمين إلى اليسار
            UIManager.put("Component.textOrientation", "RIGHT_TO_LEFT");
            UIManager.put("OptionPane.messageDialogTitle", "رسالة");
            
            // تخصيص بعض الألوان والخصائص
            UIManager.put("TabbedPane.selectedBackground", new Color(33, 150, 243)); // لون التبويب المحدد
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE); // لون النص في التبويب المحدد
            UIManager.put("Button.arc", 8); // تدوير حواف الأزرار
            UIManager.put("Component.arc", 8); // تدوير حواف العناصر
            
            // تعيين الخط الافتراضي
            Font defaultFont = new Font("Arial", Font.PLAIN, 14);
            UIManager.put("defaultFont", defaultFont);
            
            // بدء التطبيق
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "حدث خطأ أثناء بدء التطبيق: " + e.getMessage(),
                "خطأ",
                JOptionPane.ERROR_MESSAGE
            );
    }
}
}