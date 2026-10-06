package module_ui;

import module_core_logic.Category;
import module_core_logic.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Category list and CRUD controls. */
public class CategoriesPanel extends JPanel {
    private final ExpenseService expenseService;
    private final DefaultListModel<Category> categoryModel = new DefaultListModel<>();
    private final JList<Category> categoryList = new JList<>(categoryModel);

    public CategoriesPanel(ExpenseService expenseService) {
        this.expenseService = expenseService;
        setLayout(new BorderLayout(0, 12));
        setBackground(AquaTheme.LIGHT_WASH);
        setBorder(AquaTheme.padding(16, 16, 16, 16));

        JPanel categorySide = AquaTheme.createCardPanel();
        categorySide.setLayout(new BorderLayout(0, 10));
        JLabel listHeading = new JLabel("CATEGORIES", SwingConstants.CENTER);
        listHeading.setFont(new Font("Arial", Font.BOLD, 17));
        listHeading.setForeground(AquaTheme.DEEP_SLATE);
        categorySide.add(listHeading, BorderLayout.NORTH);

        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryList.setFixedCellHeight(36);
        JScrollPane categoryScroll = new JScrollPane(categoryList);
        categoryScroll.setBorder(BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER));
        categorySide.add(categoryScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 2, 6, 6));
        actions.setOpaque(false);
        JButton addButton = AquaTheme.createCyanButton("Add");
        JButton editButton = AquaTheme.createCyanButton("Edit");
        JButton deleteButton = AquaTheme.createDangerButton("Delete");
        JButton refreshButton = AquaTheme.createCyanButton("Refresh");
        addButton.addActionListener(e -> showCategoryDialog(null));
        editButton.addActionListener(e -> editSelectedCategory());
        deleteButton.addActionListener(e -> deleteSelectedCategory());
        refreshButton.addActionListener(e -> refreshCategories());
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        categorySide.add(actions, BorderLayout.SOUTH);
        add(categorySide, BorderLayout.CENTER);
        refreshCategories();
    }

    public void refreshCategories() {
        int selectedId = categoryList.getSelectedValue() == null ? -1 : categoryList.getSelectedValue().getId();
        categoryModel.clear();
        for (Category category : expenseService.getCategories()) categoryModel.addElement(category);
        if (categoryModel.isEmpty()) return;
        int selection = 0;
        for (int i = 0; i < categoryModel.size(); i++) {
            if (categoryModel.get(i).getId() == selectedId) { selection = i; break; }
        }
        categoryList.setSelectedIndex(selection);
    }

    private void editSelectedCategory() {
        Category selected = categoryList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a category to edit.", "Edit Category",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        showCategoryDialog(selected);
    }

    private void showCategoryDialog(Category existing) {
        JTextField nameField = new JTextField(existing == null ? "" : existing.getName(), 20);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"EXPENSE", "INCOME"});
        if (existing != null) typeBox.setSelectedItem(existing.getType());

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("Category Name:"));
        form.add(nameField);
        form.add(new JLabel("Category Type:"));
        form.add(typeBox);
        int choice = JOptionPane.showOptionDialog(this, form,
                existing == null ? "Add Category" : "Edit Category",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Save", "Cancel"}, "Save");
        if (choice != 0) return;

        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (type == null || type.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a category type.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (existing == null) {
                Category created = expenseService.createCategory(name, type);
                refreshCategories();
                selectCategory(created.getId());
            } else {
                Category updated = new Category(existing.getId(), name, type);
                if (expenseService.updateCategory(updated)) {
                    refreshCategories();
                    selectCategory(updated.getId());
                }
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Category Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not save category: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCategory() {
        Category selected = categoryList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Delete Category",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this category?", "Delete Category",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            if (expenseService.deleteCategory(selected.getId())) {
                refreshCategories();
            } else {
                JOptionPane.showMessageDialog(this,
                        "This category cannot be deleted because expenses are associated with it.",
                        "Category In Use", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not delete category: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectCategory(int categoryId) {
        for (int i = 0; i < categoryModel.size(); i++) {
            if (categoryModel.get(i).getId() == categoryId) {
                categoryList.setSelectedIndex(i);
                categoryList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

}
