package br.com.autogeral.javanfe.javanfeteste;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.PessoaEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad;
import br.com.swconsultoria.nfe.util.RetornoUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author muril
 */
public class ConsultaCadastroTeste {

    public static void main(String[] args) {
        try {

            File file = new File("");
            byte[] bytes = Files.readAllBytes(file.toPath());
            String senha = "";

            Certificado certificado = CertificadoService.certificadoPfxBytes(bytes, senha);

            ConfiguracoesNfe configuracoesNfe = ConfiguracoesNfe.criarConfiguracoes(EstadosEnum.SP, AmbienteEnum.HOMOLOGACAO, certificado, "");

            configuracoesNfe.setEncode("UTF-8");
            // Inicia As Configurações (1)

            //G3 03685309000150
            //Envia a Consulta
            TRetConsCad retorno = Nfe.consultaCadastro(configuracoesNfe, PessoaEnum.JURIDICA, "03685309000150", EstadosEnum.SP);

            //Valida o Retorno da Consulta Cadastro
            RetornoUtil.validaConsultaCadastro(retorno);

            //Resultado
            System.out.println();
            System.out.println("# Status: " + retorno.getInfCons().getCStat() + " - " + retorno.getInfCons().getXMotivo());
            System.out.println();
            retorno.getInfCons().getInfCad().forEach(cadastro -> {
                System.out.println("# Razão Social: " + cadastro.getXNome());
                System.out.println("# Cnpj: " + cadastro.getCNPJ());
                System.out.println("# Ie: " + cadastro.getIE());
            });

        } catch (CertificadoException | NfeException | IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Erro ao se comunicar com a SEFAZ", e);
        }
    }
}
