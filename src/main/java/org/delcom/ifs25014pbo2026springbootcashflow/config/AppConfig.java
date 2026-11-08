package org.delcom.ifs25014pbo2026springbootcashflow.config;

import org.delcom.ifs25014pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25014pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25014pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25014pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi Spring untuk merangkai komponen domain, usecase, dan adapter.
 *
 * <p>Kelas domain dan usecase tetap murni Java tanpa anotasi Spring; seluruh wiring
 * dilakukan secara eksplisit di kelas konfigurasi ini.</p>
 */
@Configuration
public class AppConfig {

    @Bean
    public ITransactionRepository transactionRepository() {
        return new TransactionRepository();
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }
}

