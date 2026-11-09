package org.delcom.ifs25014pbo2026springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.delcom.ifs25014pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25014pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25014pbo2026springbootcashflow.framework.view.FinanceView;
import org.delcom.ifs25014pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class AppTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndExposesCleanArchitectureBeans() {
        assertNotNull(context.getBean(ITransactionRepository.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));

        App app = new App();
        AppShellSettings settings = mock(AppShellSettings.class);
        app.configurePage(settings);
        verify(settings).setPageTitle("Cashflow: Catatan Keuangan");

        ServiceInitEvent serviceInitEvent = mock(ServiceInitEvent.class);
        app.serviceInit(serviceInitEvent);
        verify(serviceInitEvent).addIndexHtmlRequestListener(any(IndexHtmlRequestListener.class));

        Document doc = Jsoup.parse("<!doctype html><html lang=\"en\"><body><div id=\"outlet\"></div></body></html>");
        IndexHtmlResponse response = mock(IndexHtmlResponse.class);
        when(response.getDocument()).thenReturn(doc);
        App.customizeIndexHtml(response);
        assertEquals("id", doc.selectFirst("html").attr("lang"));
        assertEquals("main", doc.getElementById("outlet").tagName());
        assertEquals("main", doc.getElementById("outlet").attr("role"));
    }

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--spring.main.web-application-type=none"};
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            App.main(args);
            springApp.verify(() -> SpringApplication.run(App.class, args));
        }
    }

    @Test
    void dotEnvLoadsAndParsesCorrectly(@TempDir Path tempDir) throws IOException {
        DotEnv.load(tempDir.resolve("nonexistent.env"));

        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # Comment line

                INVALID_LINE_NO_EQUALS
                =EMPTY_KEY
                TEST_CASHFLOW_KEY_1=value1
                TEST_CASHFLOW_KEY_2="quoted_double"
                TEST_CASHFLOW_KEY_3='quoted_single'
                TEST_CASHFLOW_KEY_4="a
                TEST_CASHFLOW_KEY_5='b"
                """);

        System.clearProperty("TEST_CASHFLOW_KEY_1");
        System.clearProperty("TEST_CASHFLOW_KEY_2");
        System.clearProperty("TEST_CASHFLOW_KEY_3");
        System.clearProperty("TEST_CASHFLOW_KEY_4");
        System.clearProperty("TEST_CASHFLOW_KEY_5");

        DotEnv.load(envFile);

        assertEquals("value1", System.getProperty("TEST_CASHFLOW_KEY_1"));
        assertEquals("quoted_double", System.getProperty("TEST_CASHFLOW_KEY_2"));
        assertEquals("quoted_single", System.getProperty("TEST_CASHFLOW_KEY_3"));
        assertEquals("\"a", System.getProperty("TEST_CASHFLOW_KEY_4"));
        assertEquals("'b\"", System.getProperty("TEST_CASHFLOW_KEY_5"));

        // Existing system property should not be overwritten
        DotEnv.parseLine("TEST_CASHFLOW_KEY_1=overwritten");
        assertEquals("value1", System.getProperty("TEST_CASHFLOW_KEY_1"));

        // Existing OS env var should not be overwritten
        String existingEnvKey = System.getenv().keySet().iterator().next();
        System.clearProperty(existingEnvKey);
        DotEnv.parseLine(existingEnvKey + "=should_not_set");
        assertNull(System.getProperty(existingEnvKey));
    }

    @Test
    void domainEntitiesAndExceptionsWorkCorrectly() {
        LocalDateTime now = LocalDateTime.of(2026, 4, 15, 10, 30);
        Transaction tx = new Transaction(1, "Gaji Bulanan", 5_000_000.0, TransactionType.INCOME, now);
        assertEquals(1, tx.getId());
        assertEquals("Gaji Bulanan", tx.getDescription());
        assertEquals(5_000_000.0, tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals(now, tx.getCreatedAt());
        assertEquals("Pemasukan", TransactionType.INCOME.getLabel());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.getLabel());

        tx.changeDescription("Gaji & Bonus");
        tx.changeAmount(6_500_000.0);
        tx.changeType(TransactionType.EXPENSE);
        assertEquals("Gaji & Bonus", tx.getDescription());
        assertEquals(6_500_000.0, tx.getAmount());
        assertEquals(TransactionType.EXPENSE, tx.getType());

        EntityNotFoundException notFound = new EntityNotFoundException(99);
        assertEquals(99, notFound.getId());
        assertTrue(notFound.getMessage().contains("99"));
    }

    @Test
    void repositoryAndUseCaseOperationsWorkCorrectly() throws Exception {
        TransactionRepository repo = new TransactionRepository();
        FinanceUseCase useCase = new FinanceUseCase(repo);

        Transaction t1 = useCase.addIncome("  Gaji Bulanan  ", 5_000_000);
        Transaction t2 = useCase.addExpense("Makan Siang", 25_000);
        Transaction t3 = useCase.addIncome("Bonus Proyek", 1_500_000);

        assertEquals(3, useCase.getAllTransactions().size());
        assertEquals("Gaji Bulanan", t1.getDescription());
        assertEquals(6_500_000.0, useCase.getTotalIncome());
        assertEquals(25_000.0, useCase.getTotalExpense());
        assertEquals(6_475_000.0, useCase.getBalance());

        // Search
        assertEquals(1, useCase.searchTransactions("makan").size());
        assertEquals(3, useCase.searchTransactions(null).size());
        assertEquals(0, useCase.searchTransactions("tidak-ada").size());

        // Sort
        List<Transaction> asc = useCase.sortTransactions(SortOption.AMOUNT_ASC);
        assertEquals(t2.getId(), asc.get(0).getId());
        List<Transaction> desc = useCase.sortTransactions(SortOption.AMOUNT_DESC);
        assertEquals(t1.getId(), desc.get(0).getId());
        List<Transaction> incomeFirst = useCase.sortTransactions(SortOption.INCOME_FIRST);
        assertEquals(TransactionType.INCOME, incomeFirst.get(0).getType());
        List<Transaction> expenseFirst = useCase.sortTransactions(SortOption.EXPENSE_FIRST);
        assertEquals(TransactionType.EXPENSE, expenseFirst.get(0).getType());

        // Update
        Transaction updated = useCase.updateTransaction(t2.getId(), "Makan Malam", 35_000.0, TransactionType.EXPENSE);
        assertEquals("Makan Malam", updated.getDescription());
        assertEquals(35_000.0, updated.getAmount());

        // Partial update with nulls
        useCase.updateTransaction(t2.getId(), null, null, null);
        Optional<Transaction> found = repo.findById(t2.getId());
        assertTrue(found.isPresent());
        assertEquals("Makan Malam", found.get().getDescription());

        // Save existing entity vs new entity in InMemoryRepository
        Transaction manual = new Transaction(999, "Manual", 10_000, TransactionType.INCOME);
        repo.save(manual);
        assertTrue(repo.findById(999).isPresent());

        // Delete
        useCase.removeTransaction(t3.getId());
        assertFalse(repo.findById(t3.getId()).isPresent());
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(9999));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransaction(9999, "Baru", 1000.0, TransactionType.INCOME));

        // Validations
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, 1000));
        assertThrows(ValidationException.class, () -> useCase.addIncome("   ", 1000));
        assertThrows(ValidationException.class, () -> useCase.addIncome("Valid", 0));
        assertThrows(ValidationException.class, () -> useCase.addExpense("Valid", -500));
        assertThrows(ValidationException.class, () -> useCase.addIncome("Valid", Double.NaN));
        assertThrows(ValidationException.class, () -> useCase.addIncome("Valid", Double.POSITIVE_INFINITY));
        assertThrows(ValidationException.class, () -> useCase.addTransaction("Valid", 1000, null));
    }

    @Test
    void presenterFormatsStringsCorrectly() {
        FinancePresenter presenter = new FinancePresenter();
        LocalDateTime created = LocalDateTime.of(2026, 4, 15, 14, 5);
        Transaction income = new Transaction(1, "Gaji Bulanan", 5_000_000, TransactionType.INCOME, created);
        Transaction expense = new Transaction(2, "Makan Siang", 25_000, TransactionType.EXPENSE, created);

        assertEquals("[1] Gaji Bulanan | Rp 5,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan Siang | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
        assertEquals("Rp 5,000,000.00", presenter.formatAmount(income));
        assertEquals("Pemasukan", presenter.formatType(income));
        assertEquals("Pengeluaran", presenter.formatType(expense));
        assertEquals("15/04/2026 14:05", presenter.formatCreatedAt(income));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
        assertTrue(presenter.showAddSuccess(income).contains("Gaji Bulanan"));
        assertNotNull(presenter.showRemoveSuccess());
        assertTrue(presenter.showRemoveFailed(5).contains("5"));
        assertNotNull(presenter.showUpdateSuccess());
        assertTrue(presenter.showUpdateFailed(7).contains("7"));
        assertTrue(presenter.showValidationError("Pesan").contains("Pesan"));
        assertNotNull(presenter.showInvalidAmount());
        assertNotNull(presenter.showInvalidId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void financeViewRendersAndHandlesInteractions() throws Exception {
        TransactionRepository repo = new TransactionRepository();
        FinanceUseCase useCase = new FinanceUseCase(repo);
        FinancePresenter presenter = new FinancePresenter();

        FinanceView view = new FinanceView(useCase, presenter);
        assertNotNull(view);

        // Exercise onAttach
        Method onAttach = FinanceView.class.getDeclaredMethod("onAttach", AttachEvent.class);
        onAttach.setAccessible(true);
        AttachEvent attachEvent = new AttachEvent(view, true);
        onAttach.invoke(view, attachEvent);

        Transaction t1 = useCase.addIncome("Gaji Bulanan", 5_000_000);
        Transaction t2 = useCase.addExpense("Makan Siang", 25_000);

        Method createTypeBadge = FinanceView.class.getDeclaredMethod("createTypeBadge", Transaction.class);
        createTypeBadge.setAccessible(true);
        Span badge1 = (Span) createTypeBadge.invoke(view, t1);
        Span badge2 = (Span) createTypeBadge.invoke(view, t2);
        assertEquals("Pemasukan", badge1.getText());
        assertEquals("Pengeluaran", badge2.getText());

        Method createActionButtons = FinanceView.class.getDeclaredMethod("createActionButtons", Transaction.class);
        createActionButtons.setAccessible(true);
        HorizontalLayout actions = (HorizontalLayout) createActionButtons.invoke(view, t1);
        assertNotNull(actions);

        Method parseAmountInput = FinanceView.class.getDeclaredMethod("parseAmountInput", String.class);
        parseAmountInput.setAccessible(true);
        assertEquals(50000.0, (double) parseAmountInput.invoke(view, " 50000 "));

        Method searchTransactions = FinanceView.class.getDeclaredMethod("searchTransactions");
        searchTransactions.setAccessible(true);
        searchTransactions.invoke(view);

        var searchFieldRef = FinanceView.class.getDeclaredField("searchField");
        searchFieldRef.setAccessible(true);
        TextField searchField = (TextField) searchFieldRef.get(view);
        searchField.setValue("Gaji");
        searchTransactions.invoke(view);
        searchField.setValue("TidakAdaTransaksi");
        searchTransactions.invoke(view);

        var sortSelectRef = FinanceView.class.getDeclaredField("sortSelect");
        sortSelectRef.setAccessible(true);
        ComboBox<SortOption> sortSelect = (ComboBox<SortOption>) sortSelectRef.get(view);
        for (SortOption opt : SortOption.values()) {
            sortSelect.setValue(opt);
        }

        try (MockedStatic<Notification> notificationMock = mockStatic(Notification.class)) {
            Notification fakeNotification = mock(Notification.class);
            notificationMock.when(() -> Notification.show(anyString(), anyInt(),
                    org.mockito.ArgumentMatchers.any(Notification.Position.class)))
                    .thenReturn(fakeNotification);

            Method deleteTransaction = FinanceView.class.getDeclaredMethod("deleteTransaction", Transaction.class);
            deleteTransaction.setAccessible(true);
            deleteTransaction.invoke(view, t2);
            // Deleting again triggers EntityNotFoundException branch
            deleteTransaction.invoke(view, t2);
        }
    }
}
