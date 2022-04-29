package com.example.finance.api.expense;

import com.example.finance.api.common.enums.StatusType;
import com.example.finance.api.common.exception.BusinessException;
import com.example.finance.api.common.exception.ExpenseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public Expense create(Expense expense) {
        expense.setStatus(StatusType.OPEN);
        return expenseRepository.save(expense);
    }

    public Expense update(Long id, Expense expense) {
        if(expense.isPaid() && expense.getDatePayment() == null) {
            throw new BusinessException("Date payment is required when expense is paid.");
        }

        if(!expense.isPaid()) {
            expense.setDatePayment(null);
        }

        var expenseSaved = findById(id);
        BeanUtils.copyProperties(expense, expenseSaved, "id", "status");
        return expenseRepository.save(expenseSaved);
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public void pay(Long id) {
        Expense expense = findById(id);

        if(expense.isPaid()) {
            throw new BusinessException("This expense has already been paid");
        }

        expense.setStatus(StatusType.PAID);
        expense.setDatePayment(LocalDateTime.now());

        expenseRepository.save(expense);
    }

    public void cancelPayment(Long id) {
        Expense expense = findById(id);

        if(!expense.isPaid()) {
            throw new BusinessException("This expense is not paid");
        }

        expense.setStatus(StatusType.OPEN);
        expense.setDatePayment(null);

        expenseRepository.save(expense);
    }

    public void deleteById(Long id) {
        boolean exists = expenseRepository.existsById(id);

        if(!exists) {
            throw new ExpenseNotFoundException(id);
        }

        expenseRepository.deleteById(id);
    }
}