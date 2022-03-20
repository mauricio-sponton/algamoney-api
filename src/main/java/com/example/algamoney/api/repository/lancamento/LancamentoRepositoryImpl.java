package com.example.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import com.example.algamoney.api.dto.LancamentoEstatisticaPessoaDTO;
import com.example.algamoney.api.dto.LancamentoEstatisticoCategoriaDTO;
import com.example.algamoney.api.dto.LancamentoEstatisticoPorDiaDTO;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projection.ResumoLancamento;



public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager manager;
	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}

	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		List<Predicate> predicates = new ArrayList<>();
				
		if(!ObjectUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(
					builder.lower(root.get("descricao")), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}
		
		if (lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(
					builder.greaterThanOrEqualTo(root.get("dataVencimento"), lancamentoFilter.getDataVencimentoDe()));
		}
		
		if (lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(
					builder.lessThanOrEqualTo(root.get("dataVencimento"), lancamentoFilter.getDataVencimentoAte()));
		}
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}

	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder.construct(ResumoLancamento.class
				, root.get("codigo"), root.get("descricao")
				, root.get("dataVencimento"), root.get("dataPagamento")
				, root.get("valor"), root.get("tipo")
				, root.get("categoria").get("nome")
				, root.get("pessoa").get("nome")));
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}
	
	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}

	@Override
	public List<LancamentoEstatisticoCategoriaDTO> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticoCategoriaDTO> criteriaQuery = builder.createQuery(LancamentoEstatisticoCategoriaDTO.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		
		criteriaQuery.select(builder.construct(LancamentoEstatisticoCategoriaDTO.class, 
				root.get("categoria"), builder.sum(root.get("valor"))));
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia= mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		criteriaQuery.where(
				builder.greaterThanOrEqualTo(root.get("dataVencimento"), primeiroDia),
				builder.lessThanOrEqualTo(root.get("dataVencimento"), ultimoDia));
		criteriaQuery.groupBy(root.get("categoria"));
		
		TypedQuery<LancamentoEstatisticoCategoriaDTO> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticoPorDiaDTO> porDia(LocalDate mesReferencia) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticoPorDiaDTO> criteriaQuery = builder.createQuery(LancamentoEstatisticoPorDiaDTO.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		
		criteriaQuery.select(builder.construct(LancamentoEstatisticoPorDiaDTO.class, 
				root.get("tipo"), root.get("dataVencimento"), builder.sum(root.get("valor"))));
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia= mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		criteriaQuery.where(
				builder.greaterThanOrEqualTo(root.get("dataVencimento"), primeiroDia),
				builder.lessThanOrEqualTo(root.get("dataVencimento"), ultimoDia));
		criteriaQuery.groupBy(root.get("tipo"), root.get("dataVencimento"));
		
		TypedQuery<LancamentoEstatisticoPorDiaDTO> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticaPessoaDTO> porPessoa(LocalDate inicio, LocalDate fim) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaPessoaDTO> criteriaQuery = builder.createQuery(LancamentoEstatisticaPessoaDTO.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		
		criteriaQuery.select(builder.construct(LancamentoEstatisticaPessoaDTO.class, 
				root.get("tipo"), root.get("pessoa"), builder.sum(root.get("valor"))));
		
		
		criteriaQuery.where(
				builder.greaterThanOrEqualTo(root.get("dataVencimento"), inicio),
				builder.lessThanOrEqualTo(root.get("dataVencimento"), fim));
		criteriaQuery.groupBy(root.get("tipo"), root.get("pessoa"));
		
		TypedQuery<LancamentoEstatisticaPessoaDTO> typedQuery = manager.createQuery(criteriaQuery);
		return typedQuery.getResultList();
	}
}
