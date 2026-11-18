package teste;

import modelo.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TesteRegras {

    private static int ok, falhas;

    private static void verifica(String regra, boolean condicao) {
        if (condicao) {
            ok++;
            System.out.println("  OK    " + regra);
        } else {
            falhas++;
            System.out.println("  FALHA " + regra);
        }
    }

    private static void recusa(String regra, Runnable acao) {
        try {
            acao.run();
            falhas++;
            System.out.println("  FALHA " + regra + " (devia ter sido recusado)");
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            ok++;
            System.out.println("  OK    " + regra + "  →  " + e.getMessage());
        }
    }

    private static final LocalDateTime AGORA = LocalDateTime.now();
    private static int ids = 100;

    public static void main(String[] args) {
        Categoria normal = new Categoria(1, "Normal", 2, new BigDecimal("2500"));
        Categoria suite = new Categoria(2, "Suite", 1, new BigDecimal("4500"));
        Residencia central = new Residencia(1, "Central", "Mista");
        Residencia norte = new Residencia(2, "Norte", "Feminina");

        System.out.println("Quartos e camas");
        Quarto q101 = central.adicionarQuarto(1, 101, normal);
        q101.adicionarCama(1);
        q101.adicionarCama(2);
        recusa("quarto não aceita mais camas do que a capacidade da categoria", () -> q101.adicionarCama(3));
        recusa("número de quarto repetido na mesma residência", () -> central.adicionarQuarto(2, 101, normal));
        verifica("o mesmo número pode existir noutra residência", norte.adicionarQuarto(3, 101, normal) != null);
        Quarto s201 = central.adicionarQuarto(4, 201, suite);
        Cama camaSuite = s201.adicionarCama(4);
        Cama cama1 = q101.getCamas().get(0);
        Cama cama2 = q101.getCamas().get(1);
        verifica("cama nova nasce DISPONIVEL", cama1.getEstado() == EstadoCama.DISPONIVEL);

        System.out.println("Gestão (administrador ↔ residência)");
        Administrador ana = new Administrador(1, "Ana", "841", "ana@uni.mz", AGORA.minusYears(1), "Sénior");
        Administrador paulo = new Administrador(2, "Paulo", "842", "paulo@uni.mz", AGORA.minusMonths(6), "Júnior");
        Administrador rita = new Administrador(3, "Rita", "843", "rita@uni.mz", AGORA.minusMonths(2), "Júnior");
        Gestao gAna = ana.gerir(central, AGORA.minusMonths(3));
        paulo.gerir(norte, AGORA.minusMonths(2));
        recusa("um administrador gere uma só residência de cada vez", () -> ana.gerir(norte, AGORA));
        recusa("uma residência tem um só administrador de cada vez", () -> rita.gerir(central, AGORA));
        verifica("administradores registados sem gestão são permitidos", rita.getResidenciasGeridas().isEmpty());
        verifica("administrador responsável de momento", central.getAdministradorAtivo().get().equals(ana));

        System.out.println("Requisições");
        Estudante carlos = new Estudante(1, "Carlos", "845", "c@e.mz", Sexo.MASCULINO, "Informática");
        Estudante maria = new Estudante(2, "Maria", "825", "m@e.mz", Sexo.FEMININO, "Medicina");
        Requisicao r1 = carlos.fazerRequisicao(1, central);
        verifica("requisição nasce PENDENTE", r1.getResultado() == ResultadoRequisicao.PENDENTE);
        recusa("só o administrador responsável decide a requisição", () -> paulo.aprovarRequisicao(r1));
        recusa("administrador sem gestão não decide", () -> rita.aprovarRequisicao(r1));
        ana.aprovarRequisicao(r1);
        verifica("requisição aprovada pelo responsável", r1.getResultado() == ResultadoRequisicao.APROVADO);
        recusa("uma requisição só é decidida uma vez", () -> ana.rejeitarRequisicao(r1));
        Requisicao r2 = maria.fazerRequisicao(2, central);
        ana.rejeitarRequisicao(r2);
        verifica("requisição rejeitada fica NAO_APROVADO", r2.getResultado() == ResultadoRequisicao.NAO_APROVADO);

        System.out.println("Contracto");
        LocalDateTime ini = AGORA, fim = AGORA.plusMonths(6);
        recusa("requisição não aprovada não gera contracto",
                () -> new Contracto(ids++, null, ini, fim, r2, cama2, ana));
        recusa("só o administrador responsável gera o contracto",
                () -> new Contracto(ids++, null, ini, fim, r1, cama1, rita));
        recusa("a cama tem de ser da residência da requisição",
                () -> new Contracto(ids++, null, ini, fim, r1, norte.getQuartos().get(0).adicionarCama(9), ana));
        recusa("data final tem de ser posterior à inicial",
                () -> new Contracto(ids++, null, fim, ini, r1, cama1, ana));
        verifica("a cama continua DISPONIVEL depois dos pedidos recusados", cama1.getEstado() == EstadoCama.DISPONIVEL);
        Contracto c1 = new Contracto(1, null, ini, fim, r1, cama1, ana);
        verifica("contracto nasce PENDENTE", c1.getEstado() == EstadoContracto.PENDENTE);
        verifica("gerar o contracto reserva a cama", cama1.getEstado() == EstadoCama.RESERVADA);
        verifica("data de assinatura em tempo real (com hora)", c1.getDataAssinatura() != null);
        verifica("contracto sem pagamento deve ser cancelado", c1.deveSerCancelado());
        recusa("a requisição só gera um contracto", () -> new Contracto(ids++, null, ini, fim, r1, cama2, ana));

        System.out.println("Pagamento");
        Pagamento p1 = new Pagamento(1, null, MetodoPagamento.MOBILE_MONEY, 3, c1);
        verifica("valorTotal = totalMeses × preço da categoria (3 × 2500 = 7500)",
                p1.getValorTotal().compareTo(new BigDecimal("7500")) == 0);
        verifica("o pagamento ativa o contracto", c1.getEstado() == EstadoContracto.ATIVO);
        verifica("o pagamento ocupa a cama", cama1.getEstado() == EstadoCama.OCUPADA);
        verifica("data do pagamento em tempo real", p1.getData() != null);
        recusa("um contracto tem no máximo um pagamento", () -> new Pagamento(ids++, null, MetodoPagamento.NUMERARIO, 1, c1));
        recusa("meses têm de ser positivos", () -> new Pagamento(ids++, null, MetodoPagamento.NUMERARIO, 0, c1));
        verifica("contracto ativo já não deve ser cancelado por falta de pagamento", !c1.deveSerCancelado());

        System.out.println("Cancelamento e cama com vários contractos (um de cada vez)");
        recusa("a cama ocupada não aceita outro contracto", () -> {
            Requisicao r = maria.fazerRequisicao(ids++, central);
            ana.aprovarRequisicao(r);
            new Contracto(ids++, null, ini, fim, r, cama1, ana);
        });
        recusa("a cama ocupada não pode ir para manutenção", cama1::colocarEmManutencao);
        recusa("só o administrador responsável cancela", () -> c1.cancelar(paulo));
        c1.cancelar(ana);
        verifica("contracto cancelado", c1.getEstado() == EstadoContracto.CANCELADO);
        verifica("cancelar liberta a cama (volta a DISPONIVEL)", cama1.getEstado() == EstadoCama.DISPONIVEL);
        recusa("não se paga um contracto cancelado", () -> new Pagamento(ids++, null, MetodoPagamento.CARTAO, 1, c1));
        Requisicao r3 = maria.fazerRequisicao(3, central);
        ana.aprovarRequisicao(r3);
        Contracto c3 = new Contracto(3, null, ini, fim, r3, cama1, ana);
        verifica("a mesma cama pode ter outro contracto depois do cancelamento", cama1.getContractos().size() == 2);
        verifica("só um contracto em curso por cama", cama1.getContractoEmCurso().get().equals(c3));
        c3.cancelar(ana);

        System.out.println("Cancelamento de contracto pendente sem pagamento");
        Requisicao r4 = carlos.fazerRequisicao(4, central);
        ana.aprovarRequisicao(r4);
        Contracto c4 = new Contracto(4, null, ini, fim, r4, cama2, ana);
        verifica("pendente sem pagamento é candidato a cancelamento", c4.deveSerCancelado());
        c4.cancelar(ana);
        verifica("depois de cancelado a cama fica DISPONIVEL", cama2.getEstado() == EstadoCama.DISPONIVEL);

        System.out.println("Manutenção da cama");
        camaSuite.colocarEmManutencao();
        verifica("cama disponível pode ir para manutenção", camaSuite.getEstado() == EstadoCama.MANUTENCAO);
        Requisicao r5 = maria.fazerRequisicao(5, central);
        ana.aprovarRequisicao(r5);
        recusa("cama em manutenção não pode ser reservada", () -> new Contracto(ids++, null, ini, fim, r5, camaSuite, ana));
        camaSuite.sairDeManutencao();
        verifica("a cama volta a DISPONIVEL", camaSuite.getEstado() == EstadoCama.DISPONIVEL);

        System.out.println("Gestão termina e muda de administrador");
        gAna.encerrar(AGORA);
        verifica("a gestão encerrada deixa a residência sem administrador", !central.getAdministradorAtivo().isPresent());
        recusa("sem administrador ativo ninguém decide requisições", () -> ana.aprovarRequisicao(carlos.fazerRequisicao(ids++, central)));
        rita.gerir(central, AGORA);
        verifica("outro administrador assume a residência", central.getAdministradorAtivo().get().equals(rita));
        verifica("a residência guarda o histórico de gestões", central.getGestoes().size() == 2);
        verifica("ana ficou livre para gerir outra residência", ana.getResidenciasGeridas().isEmpty());

        System.out.println("Utentes independentes");
        Utente e1 = new Estudante(1, "E", "1", "e@x.mz", Sexo.FEMININO, "X");
        Utente a1 = new Administrador(1, "A", "1", "a@x.mz", AGORA, "G");
        verifica("estudante 1 e administrador 1 não são iguais", !e1.equals(a1));

        System.out.println();
        System.out.println(ok + " verificações certas, " + falhas + " falhas");
        System.exit(falhas == 0 ? 0 : 1);
    }
}