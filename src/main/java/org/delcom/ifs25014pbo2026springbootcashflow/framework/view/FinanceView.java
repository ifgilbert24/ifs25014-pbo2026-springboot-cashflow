package org.delcom.ifs25014pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.Comparator;
import java.util.List;
import org.delcom.ifs25014pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014pbo2026springbootcashflow.usecase.FinanceUseCase;

/**
 * Tampilan web Vaadin untuk aplikasi Catatan Keuangan (Cashflow).
 *
 * <p>Mendukung penambahan pemasukan/pengeluaran, pencarian berdasarkan keterangan,
 * pengurutan berdasarkan jumlah dan tipe transaksi, pengubahan transaksi dengan
 * double-klik baris, penghapusan dengan dialog konfirmasi, serta ringkasan saldo real-time.</p>
 */
@Route("")
@PageTitle("Cashflow - Catatan Keuangan")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortSelect = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span("- Data transaksi belum tersedia!");
    private final Span totalCount = new Span("0");
    private final Span totalIncomeLabel = new Span("Rp 0.00");
    private final Span totalExpenseLabel = new Span("Rp 0.00");
    private final Span balanceLabel = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Cashflow — Catatan Keuangan");

        configureGrid();
        configureToolbar();

        add(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", totalIncomeLabel, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", totalExpenseLabel, "#FEE2E2", "#DC2626", "#991B1B"),
                createSummaryCard("Saldo Saat Ini", balanceLabel, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String accent,
            String valueColor
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.2")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary(List<Transaction> allTransactions) {
        totalCount.setText(String.valueOf(allTransactions.size()));
        totalIncomeLabel.setText(presenter.formatCurrency(financeUseCase.getTotalIncome()));
        totalExpenseLabel.setText(presenter.formatCurrency(financeUseCase.getTotalExpense()));
        balanceLabel.setText(presenter.formatCurrency(financeUseCase.getBalance()));
    }

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(
                        Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(presenter::formatAmount)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingDouble(Transaction::getAmount));

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
    }

    private Span createTypeBadge(Transaction transaction) {
        boolean isIncome = transaction.getType() == TransactionType.INCOME;
        Span badge = new Span(presenter.formatType(transaction));
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.2rem 0.65rem")
                .set("border-radius", "9999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("background", isIncome ? "#DCFCE7" : "#FEE2E2")
                .set("color", isIncome ? "#166534" : "#991B1B");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(),
                event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(option -> switch (option) {
            case AMOUNT_ASC -> "Jumlah: Terkecil ke Terbesar";
            case AMOUNT_DESC -> "Jumlah: Terbesar ke Terkecil";
            case INCOME_FIRST -> "Tipe: Pemasukan Dulu";
            case EXPENSE_FIRST -> "Tipe: Pengeluaran Dulu";
        });
        sortSelect.setClearButtonVisible(true);
        sortSelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                List<Transaction> sorted = financeUseCase.sortTransactions(event.getValue());
                refreshGrid(sorted, "Daftar Transaksi (Diurutkan)");
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(),
                event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(),
                event -> refreshAll());

        Button addIncomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.INCOME));
        addIncomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button addExpenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        addExpenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                sortSelect,
                refreshButton,
                addIncomeButton,
                addExpenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("Contoh: 50000");
        amountField.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(descriptionField, amountField);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            double amount;
            try {
                amount = parseAmountInput(amountField.getValue());
            } catch (NumberFormatException _) {
                showError(presenter.showInvalidAmount());
                return;
            }
            try {
                Transaction created = type == TransactionType.INCOME
                        ? financeUseCase.addIncome(descriptionField.getValue(), amount)
                        : financeUseCase.addExpense(descriptionField.getValue(), amount);
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        String dialogTitle = type == TransactionType.INCOME
                ? "Tambah Pemasukan"
                : "Tambah Pengeluaran";
        DialogTone tone = type == TransactionType.INCOME ? DialogTone.SUCCESS : DialogTone.DANGER;
        buildDialog(dialog, dialogTitle, tone, formLayout, cancelButton, saveButton);
        dialog.open();
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan Baru");
        descriptionField.setValue(transaction.getDescription());
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah Baru (Rp)");
        amountField.setValue(String.valueOf(transaction.getAmount()));
        amountField.setWidthFull();

        ComboBox<TransactionType> typeField = new ComboBox<>("Tipe Transaksi");
        typeField.setItems(TransactionType.values());
        typeField.setItemLabelGenerator(TransactionType::getLabel);
        typeField.setValue(transaction.getType());
        typeField.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(descriptionField, amountField, typeField);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            double amount;
            try {
                amount = parseAmountInput(amountField.getValue());
            } catch (NumberFormatException _) {
                showError(presenter.showInvalidAmount());
                return;
            }
            try {
                financeUseCase.updateTransaction(
                        transaction.getId(),
                        descriptionField.getValue(),
                        amount,
                        typeField.getValue()
                );
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Mengubah Transaksi #" + transaction.getId(), DialogTone.PRIMARY,
                formLayout, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", DialogTone.DANGER, message, cancelButton, confirmButton);
        dialog.open();
    }

    private double parseAmountInput(String rawAmount) {
        if (rawAmount == null || rawAmount.trim().isEmpty()) {
            throw new NumberFormatException("Kosong");
        }
        return Double.parseDouble(rawAmount.trim());
    }

    private enum DialogTone {
        PRIMARY,
        SUCCESS,
        DANGER
    }

    private void buildDialog(
            Dialog dialog,
            String title,
            DialogTone tone,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("cashflow-form-dialog");

        String headerBackground = switch (tone) {
            case DANGER -> "#DC2626";
            case SUCCESS -> "#16A34A";
            case PRIMARY -> "#2563EB";
        };

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", headerBackground)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    private void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                          content.style.overflow = 'visible';
                          content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Transaction> results = financeUseCase.searchTransactions(keyword);
        if (results.isEmpty()) {
            emptyState.setText("- Transaksi tidak ditemukan!");
            refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
        } else {
            emptyState.setText("- Data transaksi belum tersedia!");
            refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
        }
    }

    private void refreshAll() {
        emptyState.setText("- Data transaksi belum tersedia!");
        refreshGrid(financeUseCase.getAllTransactions(), "Daftar Transaksi");
        searchField.clear();
        sortSelect.clear();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary(financeUseCase.getAllTransactions());
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

