package br.com.autogeral.javanfe.javanfeteste;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.StatusEnum;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import br.com.swconsultoria.nfe.util.ChaveUtil;
import br.com.swconsultoria.nfe.util.ConstantesUtil;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.enviNFe.*;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.*;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.COFINS;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.COFINS.COFINSAliq;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.ICMS;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.PIS;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.PIS.PISAliq;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Prod;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Total.ICMSTot;
import br.com.swconsultoria.nfe.util.RetornoUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;


import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EnvioNfeCascoBateriaTeste {

    private final ConfiguracoesNfe config;

    public EnvioNfeCascoBateriaTeste(ConfiguracoesNfe configuracoesNfe) {
        this.config = configuracoesNfe;
    }

    public static void main(String args[]) {
        File file = new File("C:/sistema/arquivo.pfx");
        if (!file.exists()) {
            System.out.println("Arquivo  : " + file.getAbsolutePath() + ", não encontrado");
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String senha = "senha_super_secreta";
            String schemas = "D:\\projects\\Java_NFe\\schemas";
            Certificado certificado = CertificadoService.certificadoPfxBytes(bytes, senha);
            ConfiguracoesNfe configuracoesNfe = ConfiguracoesNfe.criarConfiguracoes(EstadosEnum.SP, AmbienteEnum.PRODUCAO, certificado, schemas);

            EnvioNfeCascoBateriaTeste enb = new EnvioNfeCascoBateriaTeste(configuracoesNfe);
            enb.enviarNotaDeCascos();
        } catch (IOException e) {
            System.out.println("Erro ao tentar manipular arquivos ou rede");
            e.printStackTrace(System.err);
        } catch (CertificadoException e) {
            System.out.println("Erro ao com o certificado");
            e.printStackTrace(System.err);
        }
    }

    public String gerarCodigoNumerico() {
        // Usa SecureRandom para maior segurança e aleatoriedade
        Random random = new SecureRandom();

        // Gera um número entre 1 e 99.999.999.
        // O limite superior é exclusivo, por isso somamos 1.
        int numeroAleatorio = random.nextInt(99999999) + 1;

        // Formata o número para ter 8 dígitos, preenchendo com zeros à esquerda
        // Ex: se o número for 123, o resultado será "00000123"
        String cnfFormatado = String.format("%08d", numeroAleatorio);

        return cnfFormatado;
    }


    private void enviarNotaDeCascos() {
        //Informe o Numero da NFe
        int numeroDocumento = 1098827;
        //Informe o CNPJ do Emitente da NFe
        String cnpj = "05437537000137";
        //Informe a data de Emissao da NFe
        LocalDateTime dataEmissao = LocalDateTime.now();
        //Informe o cnf da NFe com 8 digitos
        String cnf = gerarCodigoNumerico();
        //Informe o modelo da NFe
        String modelo = DocumentoEnum.NFE.getModelo();
        //Informe a Serie da NFe
        int serie = 1;
        //Informe o tipo de Emissao da NFe
        String tipoEmissao = "1";
        ChaveUtil chaveUtil = new ChaveUtil(config.getEstado(), cnpj, modelo, serie, numeroDocumento, tipoEmissao, cnf, dataEmissao);
        String chave = chaveUtil.getChaveNF();
        String cdv = chaveUtil.getDigitoVerificador();

        TNFe.InfNFe infNFe = new TNFe.InfNFe();
        infNFe.setId(chave);
        infNFe.setVersao(ConstantesUtil.VERSAO.NFE);

        //Preenche IDE
        infNFe.setIde(preencheIde(config, cnf, numeroDocumento, tipoEmissao, modelo, serie, cdv, dataEmissao));

        //Preenche Emitente
        infNFe.setEmit(preencheEmitente(config, cnpj));

        //Preenche o Destinatario
        infNFe.setDest(preencheDestinatario());

        //Preenche os dados do Produto da Nfe e adiciona a Lista
        infNFe.getDet().addAll(preencheDet());

        //Preenche totais da NFe
        infNFe.setTotal(preencheTotal());

        //Preenche os dados de Transporte
        infNFe.setTransp(preencheTransporte());

        // Preenche dados Pagamento
        infNFe.setPag(preenchePag());

        TNFe nfe = new TNFe();
        nfe.setInfNFe(infNFe);

        TNFe.InfNFe.InfAdic infAdic = new TNFe.InfNFe.InfAdic();
        infAdic.setInfCpl("BATERIAS USADAS INSERVIVEIS RICARDO, Isento conforme Artigo 119, ICMS");
        infNFe.setInfAdic(infAdic);

        nfe.setInfNFe(infNFe);
        long now = Instant.now().toEpochMilli();
        String loteId = Long.toString(now);
        TEnviNFe enviNFe = new TEnviNFe();
        enviNFe.setVersao(ConstantesUtil.VERSAO.NFE);
        enviNFe.setIdLote(loteId);
        enviNFe.setIndSinc("1");
        enviNFe.getNFe().add(nfe);


        try {
            String xml = XmlNfeUtil.objectToXml(enviNFe, config.getEncode());

            File file = new File("env" + chave + "-00.xml");
            Files.write(file.toPath(), xml.getBytes(), java.nio.file.StandardOpenOption.CREATE);

            // Monta e Assina o XML
            enviNFe = Nfe.montaNfe(config, enviNFe, true);

            xml = XmlNfeUtil.objectToXml(enviNFe, config.getEncode());
            file = new File("env" + chave + "-01.xml");
            Files.write(file.toPath(), xml.getBytes(), java.nio.file.StandardOpenOption.CREATE);

            // Envia a Nfe para a Sefaz
            TRetEnviNFe retorno = Nfe.enviarNfe(config, enviNFe, DocumentoEnum.NFE);

            xml = XmlNfeUtil.objectToXml(retorno, config.getEncode());
            file = new File("env-" + chave + "-ret.xml");
            Files.write(file.toPath(), xml.getBytes(), java.nio.file.StandardOpenOption.CREATE);

            //Valida se o Retorno é Assincrono
            if (RetornoUtil.isRetornoAssincrono(retorno)) {
                //Pega o Recibo
                String recibo = retorno.getInfRec().getNRec();
                int tentativa = 0;
                br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe retornoNfe = null;

                //Define Numero de tentativas que irá tentar a Consulta
                while (tentativa < 15) {
                    retornoNfe = Nfe.consultaRecibo(config, recibo, DocumentoEnum.NFE);
                    if (retornoNfe.getCStat().equals(StatusEnum.LOTE_EM_PROCESSAMENTO.getCodigo())) {
                        System.out.println("INFO: Lote Em Processamento, vai tentar novamente apos 1 Segundo.");
                        Thread.sleep(1000);
                        tentativa++;
                    } else {
                        break;
                    }
                }

                xml = XmlNfeUtil.objectToXml(retornoNfe, config.getEncode());
                file = new File("env-" + chave + "-ret-nfe.xml");
                Files.write(file.toPath(), xml.getBytes(), java.nio.file.StandardOpenOption.CREATE);

                RetornoUtil.validaAssincrono(retornoNfe);
                System.out.println();
                System.out.println("# Status: " + retornoNfe.getProtNFe().get(0).getInfProt().getCStat() + " - " + retornoNfe.getProtNFe().get(0).getInfProt().getXMotivo());
                System.out.println("# Protocolo: " + retornoNfe.getProtNFe().get(0).getInfProt().getNProt());
                System.out.println("# XML Final: " + XmlNfeUtil.criaNfeProc(enviNFe, retornoNfe.getProtNFe().get(0)));

            } else {
                //Se for else o Retorno é Sincrono
                xml = XmlNfeUtil.objectToXml(retorno, config.getEncode());
                file = new File("env-" + chave + "-ret-nfe.xml");
                Files.write(file.toPath(), xml.getBytes(), java.nio.file.StandardOpenOption.CREATE);

                //Valida Retorno Sincrono
                RetornoUtil.validaSincrono(retorno);


                System.out.println();
                System.out.println("# Status: " + retorno.getProtNFe().getInfProt().getCStat() + " - " + retorno.getProtNFe().getInfProt().getXMotivo());
                System.out.println("# Protocolo: " + retorno.getProtNFe().getInfProt().getNProt());
                System.out.println("# Xml Final :" + XmlNfeUtil.criaNfeProc(enviNFe, retorno.getProtNFe()));
            }
        } catch (JAXBException e) {
            System.out.println("Erro ao tentar manipular o XML");
            e.printStackTrace(System.err);
        } catch (NfeException e) {
            System.out.println("Erro da biblioteca da NFe");
            e.printStackTrace(System.err);
        } catch (InterruptedException e) {
            System.out.println("Erro de Threads");
            e.printStackTrace(System.err);
        } catch (IOException e) {
            System.out.println("Erro ao tentar escrever XML em arquivo");
            e.printStackTrace(System.err);
        }
    }

    private Ide preencheIde(ConfiguracoesNfe config, String cnf, int numeroDocumento, String tipoEmissao, String modelo, int serie, String cDv, LocalDateTime dataEmissao) {
        Ide ide = new Ide();
        ide.setCUF("35");
        ide.setCNF(cnf);
        ide.setNatOp("REMESSA DE BATERIAS INSERVIVEIS");
        ide.setMod(modelo);
        ide.setSerie(String.valueOf(serie));

        ide.setNNF(String.valueOf(numeroDocumento));
        ide.setDhEmi(XmlNfeUtil.dataNfe(dataEmissao));
        ide.setTpNF("1"); // SEMPRE SERA 1 POR SE TRATAR DE NOTAS DE SAIDAS
        ide.setIdDest("1"); // SEMPRE VAI SER 1 POR SE TRATAR DE NOTAS PARA O MESMO ESTADO
        ide.setCMunFG("3523909");
        /**
         * 1 - Retrato (padrão).
         * 2 - Paisagem.
         * 3 - DANFE Simplificado.
         * 4 - DANFE NFC-e (Nota Fiscal de Consumidor Eletrônica) em formato de Bobina.
         */
        ide.setTpImp("1"); // NFCE COM DANFE
        ide.setTpEmis(tipoEmissao);
        ide.setCDV(cDv);
        ide.setTpAmb(config.getAmbiente().getCodigo());
        ide.setFinNFe("1"); // PADRAO
        ide.setIndFinal("1"); // PADRAO
        ide.setIndPres("1"); // PADRAO
        ide.setProcEmi("0"); // PADRAO
        ide.setVerProc("1.0"); // PADRAO
        return ide;
    }

    private static Emit preencheEmitente(ConfiguracoesNfe config, String cnpj) {
        Emit emit = new Emit();
        emit.setCNPJ(cnpj);
        emit.setXNome("AUTO GERAL AUTOPECAS LTDA");
        emit.setXFant("AUTO GERAL");

        TEnderEmi enderEmit = new TEnderEmi();
        enderEmit.setXLgr("AV. DR. OCTAVIANO PEREIRA MENDES");
        enderEmit.setNro("1333");
        enderEmit.setXBairro("Centro");
        enderEmit.setCMun("3523909");
        enderEmit.setXMun("ITU");
        enderEmit.setUF(TUfEmi.valueOf(config.getEstado().toString()));
        enderEmit.setCEP("13301909");
        enderEmit.setCPais("1058");
        enderEmit.setXPais("Brasil");
        enderEmit.setFone("1140137777");
        emit.setEnderEmit(enderEmit);

        emit.setIE("387034155115");
        emit.setIM("2623");
        emit.setCNAE("4530701");
        emit.setCRT("3");
        return emit;
    }

    private static Dest preencheDestinatario() {
        Dest dest = new Dest();
        dest.setCNPJ("46210258000113");
        dest.setXNome("COMERCIAL BANDEIRANTES DE BATERIAS LTDA");

        TEndereco enderDest = new TEndereco();
        enderDest.setXLgr("AVENIDA DOUTOR EDUARDO PEREIRA DE ALMEIDA");
        enderDest.setNro("47");
        enderDest.setXBairro("Real Parque");
        enderDest.setCMun("3509502");
        enderDest.setXMun("CAMPINAS");
        enderDest.setUF(TUf.valueOf("SP"));
        enderDest.setCEP("13082782");
        enderDest.setCPais("1058");
        enderDest.setXPais("Brasil");
        enderDest.setFone("01937498830");
        dest.setEnderDest(enderDest);

        dest.setEmail("evelin@moura15.com.br");
        dest.setIndIEDest("1");
        dest.setIE("244194451111");
        return dest;
    }

    private static List<Det> preencheDet() {

        //O Preenchimento deve ser feito por produto, Então deve ocorrer uma LIsta
        Det det = new Det();
        //O numero do Item deve seguir uma sequencia
        det.setNItem("1");

        // Preenche dados do Produto
        det.setProd(preencheProduto());

        //Preenche dados do Imposto
        det.setImposto(preencheImposto());

        //Retorna a Lista de Det
        return Collections.singletonList(det);
    }

    private static Prod preencheProduto() {
        Prod prod = new Prod();
        prod.setCProd("100000");
        prod.setCEAN("SEM GTIN");
        prod.setXProd("CASCO BATERIA");
        prod.setNCM("85491100");
//        prod.setIndEscala("S");
        prod.setCFOP("5949");
        prod.setUCom("KG");
        prod.setQCom("1103.3500");
        prod.setVUnCom("0.01");
        prod.setVProd("11.03");
        prod.setCEANTrib("SEM GTIN");
        prod.setUTrib("KG");
        prod.setQTrib("1103.3500");
        prod.setVUnTrib("0.01");
        prod.setIndTot("1");

        return prod;
    }

    private static Imposto preencheImposto() {
        Imposto imposto = new Imposto();

        ICMS icms = new ICMS();

        ICMS.ICMS40 icms40 = new ICMS.ICMS40();
        icms40.setOrig("0");
        icms40.setCST("40");

        icms.setICMS40(icms40);

        PIS pis = new PIS();
        PIS.PISNT pisNt = new PIS.PISNT();
        pisNt.setCST("07");
        pis.setPISNT(pisNt);
//        PISAliq pisAliq = new PISAliq();
//        pisAliq.setCST("07");
//        pis.setPISAliq(pisAliq);

        COFINS cofins = new COFINS();
        COFINS.COFINSNT cofinsNt = new COFINS.COFINSNT();
        cofinsNt.setCST("07");
        cofins.setCOFINSNT(cofinsNt);
//        COFINSAliq cofinsAliq = new COFINSAliq();
//        cofinsAliq.setCST("07");
//        cofins.setCOFINSAliq(cofinsAliq);

        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoICMS(icms));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoPIS(pis));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoCOFINS(cofins));

        return imposto;
    }

    private static Total preencheTotal() {
        Total total = new Total();
        ICMSTot icmstot = new ICMSTot();
        icmstot.setVBC("0.00");
        icmstot.setVICMS("0.00");
        icmstot.setVICMSDeson("0.00");
        icmstot.setVFCP("0.00");
        icmstot.setVFCPST("0.00");
        icmstot.setVFCPSTRet("0.00");
        icmstot.setVBCST("0.00");
        icmstot.setVST("0.00");
        icmstot.setVProd("11.03");
        icmstot.setVFrete("0.00");
        icmstot.setVSeg("0.00");
        icmstot.setVDesc("0.00");
        icmstot.setVII("0.00");
        icmstot.setVIPI("0.00");
        icmstot.setVIPIDevol("0.00");
        icmstot.setVPIS("0.00");
        icmstot.setVCOFINS("0.00");
        icmstot.setVOutro("0.00");
        icmstot.setVNF("11.03");
        total.setICMSTot(icmstot);
        return total;
    }

    private static Transp preencheTransporte(){
        Transp transp = new Transp();
        transp.setModFrete("9");
        return transp;
    }

    private static Pag preenchePag() {
        Pag pag = new Pag();
        Pag.DetPag detPag = new Pag.DetPag();
        detPag.setIndPag("1");
        detPag.setTPag("99");
        detPag.setXPag("GARANTIA");
        detPag.setVPag("11.03");
        pag.getDetPag().add(detPag);
        return pag;
    }
}
