package com.example.finance.api.expense;

import com.example.finance.api.common.enums.StatusType;
import com.example.finance.api.common.exception.ExpenseNotFoundException;
import com.example.finance.api.planning.Planning;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public Expense create(Expense expense) {
        expense.create();
        return expenseRepository.save(expense);
    }

    public Expense update(Long id, Expense expense) {
        expense.update();
        var expenseSaved = findById(id);
        BeanUtils.copyProperties(expense, expenseSaved, "id", "status", "version", "createdAt", "updatedAt");
        return expenseRepository.save(expenseSaved);
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public void pay(Long id) {
        Expense expense = findById(id);

        expense.pay();

        expenseRepository.save(expense);
    }

    public void cancelPayment(Long id) {
        Expense expense = findById(id);

        expense.cancelPayment();

        expenseRepository.save(expense);
    }

    public void deleteById(Long id) {
        boolean exists = expenseRepository.existsById(id);

        if(!exists) {
            throw new ExpenseNotFoundException(id);
        }

        expenseRepository.deleteById(id);
    }

    public Expense buildByPlanning(Planning planning, LocalDate dateDue) {
        return Expense.builder()
                .status(StatusType.OPEN)
                .dateDue(dateDue)
                .description(planning.getDescription())
                .amount(planning.getAmount())
                .planning(planning)
                .build();
    }

    @Transactional
    public void create(List<Expense> expenses) {
        expenses.forEach(this::create);
    }
}
