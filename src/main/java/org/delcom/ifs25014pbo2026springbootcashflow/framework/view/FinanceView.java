package org.delcom.ifs25014pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import java.util.Locale;
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
 * <p>Dirancang dengan gaya minimalis buku kas ala Linear, memenuhi standar aksesibilitas
 * WCAG AAA (kontras minimal 7:1), landmark semantik HTML5 ({@code header}, {@code main},
 * {@code section}), serta dukungan penuh navigasi keyboard.</p>
 */
@Route("")
@PageTitle("Cashflow: Catatan Keuangan")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortSelect = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(
            "Belum ada catatan transaksi. Gunakan tombol Tambah Pemasukan atau Tambah Pengeluaran di atas untuk mencatat transaksi pertama."
    );
    private final Span totalCount = new Span("0");
    private final Span totalIncomeLabel = new Span("Rp 0.00");
    private final Span totalExpenseLabel = new Span("Rp 0.00");
    private final Span balanceLabel = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background-color", "#F8FAFC")
                .set("color", "#0F172A")
                .set("font-family", "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif");

        configureGrid();
        configureToolbar();

        Header pageHeader = buildHeader();
        Main mainContent = buildMainContent();

        add(pageHeader, mainContent);
        setFlexGrow(1, mainContent);
        refreshAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        if (ui != null) {
            ui.setLocale(Locale.forLanguageTag("id-ID"));
            ui.getPage().executeJs("""
                    document.documentElement.setAttribute('lang', 'id');
                    if (!document.getElementById('cashflow-a11y-styles')) {
                        const style = document.createElement('style');
                        style.id = 'cashflow-a11y-styles';
                        style.textContent = `
                            :root {
                                --lumo-primary-text-color: #1E40AF;
                                --lumo-primary-color: #1E40AF;
                                --lumo-success-text-color: #166534;
                                --lumo-success-color: #15803D;
                                --lumo-error-text-color: #991B1B;
                                --lumo-error-color: #B91C1C;
                                --lumo-body-text-color: #0F172A;
                                --lumo-secondary-text-color: #334155;
                                --lumo-tertiary-text-color: #475569;
                                --lumo-contrast-60pct: #475569;
                                --lumo-contrast-70pct: #334155;
                                --lumo-contrast-80pct: #1E293B;
                                --lumo-contrast-90pct: #0F172A;
                            }
                            a[href], button, summary, select, label[for],
                            [role="button"], [role="link"], [role="tab"], [role="menuitem"],
                            [role="option"], [role="switch"], vaadin-button, vaadin-combo-box {
                                cursor: pointer;
                            }
                            :disabled, [aria-disabled="true"], vaadin-button[disabled] {
                                cursor: not-allowed;
                            }
                            vaadin-button {
                                min-height: 44px;
                                font-weight: 600;
                            }
                            vaadin-text-field::part(input-field),
                            vaadin-combo-box::part(input-field) {
                                min-height: 44px;
                                background-color: #FFFFFF;
                                border: 1px solid #94A3B8;
                            }
                            vaadin-button:focus-visible,
                            vaadin-text-field:focus-within,
                            vaadin-combo-box:focus-within {
                                outline: 2px solid #1E40AF;
                                outline-offset: 2px;
                            }
                            @media (prefers-reduced-motion: reduce) {
                                *, *::before, *::after {
                                    animation-duration: 0.01ms !important;
                                    animation-iteration-count: 1 !important;
                                    transition-duration: 0.01ms !important;
                                    scroll-behavior: auto !important;
                                }
                            }
                        `;
                        document.head.appendChild(style);
                    }
                    """);
        }
    }

    private Header buildHeader() {
        H1 title = new H1("Cashflow: Catatan Keuangan");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "clamp(1.25rem, 2.2vw, 1.5rem)")
                .set("font-weight", "700")
                .set("letter-spacing", "-0.02em")
                .set("color", "#0F172A");

        Paragraph subtitle = new Paragraph(
                "Buku kas pencatatan pemasukan dan pengeluaran harian. Klik ganda pada baris tabel untuk memperbarui transaksi."
        );
        subtitle.getStyle()
                .set("margin", "0.25rem 0 0 0")
                .set("font-size", "0.925rem")
                .set("color", "#334155");

        Div container = new Div(title, subtitle);
        container.getStyle()
                .set("max-width", "1280px")
                .set("width", "100%")
                .set("margin", "0 auto")
                .set("padding", "1.25rem 1.5rem")
                .set("box-sizing", "border-box");

        Header header = new Header(container);
        header.getElement().setAttribute("role", "banner");
        header.getStyle()
                .set("width", "100%")
                .set("background-color", "#FFFFFF")
                .set("border-bottom", "1px solid #CBD5E1");
        return header;
    }

    private Main buildMainContent() {
        Section summarySection = new Section(buildSummaryCards());
        summarySection.getElement().setAttribute("aria-label", "Ringkasan Keuangan");
        summarySection.getStyle().set("width", "100%");

        Section controlsSection = new Section(buildToolbar());
        controlsSection.getElement().setAttribute("aria-label", "Pencarian, Pengurutan, dan Tindakan Transaksi");
        controlsSection.getStyle()
                .set("width", "100%")
                .set("margin-top", "1.25rem");

        H2 tableHeading = new H2("Daftar Catatan Transaksi");
        tableHeading.getStyle()
                .set("margin", "0")
                .set("font-size", "1.05rem")
                .set("font-weight", "600")
                .set("color", "#0F172A");

        statusLabel.getElement().setAttribute("role", "status");
        statusLabel.getElement().setAttribute("aria-live", "polite");
        statusLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", "#334155");

        Div tableHeaderBar = new Div(tableHeading, statusLabel);
        tableHeaderBar.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("justify-content", "space-between")
                .set("align-items", "baseline")
                .set("gap", "0.5rem")
                .set("margin-bottom", "0.75rem");

        Section tableSection = new Section(tableHeaderBar, grid);
        tableSection.getElement().setAttribute("aria-label", "Tabel Catatan Transaksi");
        tableSection.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("flex", "1")
                .set("min-height", "360px")
                .set("width", "100%")
                .set("margin-top", "1.25rem");

        Div container = new Div(summarySection, controlsSection, tableSection);
        container.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("flex", "1")
                .set("max-width", "1280px")
                .set("width", "100%")
                .set("margin", "0 auto")
                .set("padding", "1.25rem 1.5rem 2rem")
                .set("box-sizing", "border-box");

        Main main = new Main(container);
        main.getElement().setAttribute("role", "main");
        main.setId("main-content");
        main.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("flex", "1")
                .set("width", "100%")
                .set("overflow-y", "auto");
        return main;
    }

    private HorizontalLayout buildSummaryCards() {
        Div balanceCard = createSummaryCard(
                "Saldo Saat Ini",
                balanceLabel,
                "#0F172A",
                "#0F172A",
                "#CBD5E1",
                "#FFFFFF",
                true
        );
        Div incomeCard = createSummaryCard(
                "Total Pemasukan",
                totalIncomeLabel,
                "#FFFFFF",
                "#CBD5E1",
                "#334155",
                "#166534",
                false
        );
        Div expenseCard = createSummaryCard(
                "Total Pengeluaran",
                totalExpenseLabel,
                "#FFFFFF",
                "#CBD5E1",
                "#334155",
                "#991B1B",
                false
        );
        Div countCard = createSummaryCard(
                "Total Transaksi",
                totalCount,
                "#FFFFFF",
                "#CBD5E1",
                "#334155",
                "#0F172A",
                false
        );

        HorizontalLayout summary = new HorizontalLayout(balanceCard, incomeCard, expenseCard, countCard);
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "1rem")
                .set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String borderColor,
            String captionColor,
            String valueColor,
            boolean isPrimaryFocal
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("color", captionColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.4rem")
                .set("font-size", isPrimaryFocal ? "clamp(1.4rem, 2.2vw, 1.75rem)" : "clamp(1.25rem, 1.8vw, 1.5rem)")
                .set("font-weight", "700")
                .set("font-variant-numeric", "tabular-nums")
                .set("line-height", "1.2")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", isPrimaryFocal ? "1.3 1 240px" : "1 1 200px")
                .set("min-width", "180px")
                .set("background-color", background)
                .set("border", "1px solid " + borderColor)
                .set("border-radius", "8px")
                .set("padding", "1rem 1.25rem")
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

        emptyState.getStyle()
                .set("display", "block")
                .set("padding", "1.5rem")
                .set("color", "#334155")
                .set("font-weight", "500");

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.getElement().setAttribute("aria-label", "Tabel Catatan Transaksi Keuangan");
        grid.setSizeFull();
        grid.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #CBD5E1")
                .set("border-radius", "8px");
    }

    private Span createTypeBadge(Transaction transaction) {
        boolean isIncome = transaction.getType() == TransactionType.INCOME;
        Span badge = new Span(presenter.formatType(transaction));
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.2rem 0.6rem")
                .set("border-radius", "4px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("border", isIncome ? "1px solid #86EFAC" : "1px solid #FCA5A5")
                .set("background-color", isIncome ? "#DCFCE7" : "#FEE2E2")
                .set("color", isIncome ? "#166534" : "#991B1B");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Icon trashIcon = createDecorativeIcon(VaadinIcon.TRASH);
        Button deleteButton = new Button("Hapus", trashIcon, event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.setAriaLabel("Hapus transaksi " + transaction.getDescription());
        deleteButton.getStyle()
                .set("color", "#991B1B")
                .set("font-weight", "600")
                .set("min-height", "36px")
                .set("padding", "0.25rem 0.5rem");
        return new HorizontalLayout(deleteButton);
    }

    private void configureToolbar() {
        searchField.setLabel("Cari Keterangan");
        searchField.setPlaceholder("Ketik kata kunci transaksi...");
        searchField.setPrefixComponent(createDecorativeIcon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.setAriaLabel("Cari keterangan transaksi");
        searchField.getStyle().set("min-width", "220px");

        sortSelect.setLabel("Urutkan Transaksi");
        sortSelect.setPlaceholder("Pilih urutan...");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(option -> switch (option) {
            case AMOUNT_ASC -> "Jumlah: Terkecil ke Terbesar";
            case AMOUNT_DESC -> "Jumlah: Terbesar ke Terkecil";
            case INCOME_FIRST -> "Tipe: Pemasukan Dulu";
            case EXPENSE_FIRST -> "Tipe: Pengeluaran Dulu";
        });
        sortSelect.setClearButtonVisible(true);
        sortSelect.setAriaLabel("Urutkan daftar transaksi");
        sortSelect.getStyle().set("min-width", "220px");
        sortSelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                List<Transaction> sorted = financeUseCase.sortTransactions(event.getValue());
                refreshGrid(sorted, "Daftar Transaksi (Diurutkan)");
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", createDecorativeIcon(VaadinIcon.SEARCH),
                event -> searchTransactions());
        searchButton.setAriaLabel("Jalankan pencarian transaksi");
        searchButton.getStyle()
                .set("background-color", "#FFFFFF")
                .set("color", "#0F172A")
                .set("border", "1px solid #94A3B8");

        Button refreshButton = new Button("Muat Ulang", createDecorativeIcon(VaadinIcon.REFRESH),
                event -> refreshAll());
        refreshButton.setAriaLabel("Muat ulang semua transaksi");
        refreshButton.getStyle()
                .set("background-color", "#FFFFFF")
                .set("color", "#0F172A")
                .set("border", "1px solid #94A3B8");

        Button addIncomeButton = new Button("Tambah Pemasukan", createDecorativeIcon(VaadinIcon.PLUS_CIRCLE),
                event -> openAddDialog(TransactionType.INCOME));
        addIncomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addIncomeButton.setAriaLabel("Tambah transaksi pemasukan baru");
        addIncomeButton.getStyle()
                .set("background-color", "#166534")
                .set("color", "#FFFFFF");

        Button addExpenseButton = new Button("Tambah Pengeluaran", createDecorativeIcon(VaadinIcon.MINUS_CIRCLE),
                event -> openAddDialog(TransactionType.EXPENSE));
        addExpenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addExpenseButton.setAriaLabel("Tambah transaksi pengeluaran baru");
        addExpenseButton.getStyle()
                .set("background-color", "#991B1B")
                .set("color", "#FFFFFF");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                sortSelect,
                refreshButton,
                addIncomeButton,
                addExpenseButton
        );
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "0.75rem")
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #CBD5E1")
                .set("border-radius", "8px")
                .set("padding", "1rem 1.25rem")
                .set("box-sizing", "border-box");
        return toolbar;
    }

    private Icon createDecorativeIcon(VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getElement().setAttribute("aria-hidden", "true");
        return icon;
    }

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setPlaceholder("Masukkan keterangan transaksi...");
        descriptionField.setRequiredIndicatorVisible(true);
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("Masukkan angka nominal (contoh: 50000)");
        amountField.setRequiredIndicatorVisible(true);
        amountField.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(descriptionField, amountField);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan Transaksi", createDecorativeIcon(VaadinIcon.CHECK), event -> {
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
        saveButton.getStyle()
                .set("background-color", type == TransactionType.INCOME ? "#166534" : "#991B1B")
                .set("color", "#FFFFFF");

        Button cancelButton = new Button("Batal", createDecorativeIcon(VaadinIcon.CLOSE),
                event -> dialog.close());
        cancelButton.getStyle().set("color", "#0F172A");

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
        descriptionField.setRequiredIndicatorVisible(true);
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah Baru (Rp)");
        amountField.setValue(String.valueOf(transaction.getAmount()));
        amountField.setRequiredIndicatorVisible(true);
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
        Button saveButton = new Button("Simpan Perubahan", createDecorativeIcon(VaadinIcon.CHECK), event -> {
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
        saveButton.getStyle()
                .set("background-color", "#1E40AF")
                .set("color", "#FFFFFF");

        Button cancelButton = new Button("Batal", createDecorativeIcon(VaadinIcon.CLOSE),
                event -> dialog.close());
        cancelButton.getStyle().set("color", "#0F172A");

        buildDialog(dialog, "Mengubah Transaksi #" + transaction.getId(), DialogTone.PRIMARY,
                formLayout, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Apakah Anda yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ")?"
        );
        message.getStyle()
                .set("margin", "0")
                .set("color", "#0F172A");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus Transaksi", createDecorativeIcon(VaadinIcon.TRASH), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.getStyle()
                .set("background-color", "#991B1B")
                .set("color", "#FFFFFF");

        Button cancelButton = new Button("Batal", createDecorativeIcon(VaadinIcon.CLOSE),
                event -> dialog.close());
        cancelButton.getStyle().set("color", "#0F172A");

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
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.setCloseOnEsc(true);
        dialog.getElement().setAttribute("aria-label", title);
        dialog.addClassName("cashflow-form-dialog");

        String headerBackground = switch (tone) {
            case DANGER -> "#991B1B";
            case SUCCESS -> "#166534";
            case PRIMARY -> "#0F172A";
        };

        H2 headerTitle = new H2(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.1rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background-color", headerBackground)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background-color", "#FFFFFF")
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
                .set("background-color", "#F8FAFC")
                .set("border-top", "1px solid #CBD5E1")
                .set("padding", "0.85rem 1.5rem 1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "8px")
                .set("background-color", "#FFFFFF");

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
            emptyState.setText(
                    "Transaksi dengan kata kunci \"" + keyword + "\" tidak ditemukan. Klik Muat Ulang untuk menampilkan semua data."
            );
            refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
        } else {
            emptyState.setText(
                    "Belum ada catatan transaksi. Gunakan tombol Tambah Pemasukan atau Tambah Pengeluaran di atas untuk mencatat transaksi pertama."
            );
            refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
        }
    }

    private void refreshAll() {
        emptyState.setText(
                "Belum ada catatan transaksi. Gunakan tombol Tambah Pemasukan atau Tambah Pengeluaran di atas untuk mencatat transaksi pertama."
        );
        refreshGrid(financeUseCase.getAllTransactions(), "Daftar Transaksi");
        searchField.clear();
        sortSelect.clear();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary(financeUseCase.getAllTransactions());
        if (transactions.isEmpty()) {
            statusLabel.setText(title + ": " + emptyState.getText());
        } else {
            statusLabel.setText(title + " (" + transactions.size() + " catatan)");
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
