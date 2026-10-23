package com.estoque.reserva.api;

import com.estoque.reserva.dominio.EstoqueInsuficienteException;
import com.estoque.reserva.dominio.OperacaoInvalidaException;
import com.estoque.reserva.dominio.RecursoNaoEncontradoException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({EstoqueInsuficienteException.class, OperacaoInvalidaException.class})
    public ProblemDetail regraDeNegocio(RuntimeException e) {
        return problema(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Conflito de @Version: outra transação alterou o produto primeiro. */
    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ProblemDetail conflitoDeVersao() {
        return problema(HttpStatus.CONFLICT, "estoque alterado por outra requisição; tente novamente");
    }

    private ProblemDetail problema(HttpStatus status, String detalhe) {
        return ProblemDetail.forStatusAndDetail(status, detalhe);
    }
}
