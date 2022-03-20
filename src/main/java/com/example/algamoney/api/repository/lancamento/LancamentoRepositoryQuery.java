package com.example.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.algamoney.api.dto.LancamentoEstatisticaPessoaDTO;
import com.example.algamoney.api.dto.LancamentoEstatisticoCategoriaDTO;
import com.example.algamoney.api.dto.LancamentoEstatisticoPorDiaDTO;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projection.ResumoLancamento;


public interface LancamentoRepositoryQuery {

	public List<LancamentoEstatisticoCategoriaDTO> porCategoria(LocalDate mesReferencia);
	public List<LancamentoEstatisticoPorDiaDTO> porDia(LocalDate mesReferencia);
	public List<LancamentoEstatisticaPessoaDTO> porPessoa(LocalDate inicio, LocalDate fim);

	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable);
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable);

}
