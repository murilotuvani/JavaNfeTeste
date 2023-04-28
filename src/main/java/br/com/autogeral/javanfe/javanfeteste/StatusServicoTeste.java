package br.com.autogeral.javanfe.javanfeteste;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 19/04/2023 &{time}
 *
 * @author muril
 */
public class StatusServicoTeste {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            File file = new File("");
            byte[] bytes = Files.readAllBytes(file.toPath());
            String senha = "";

            Certificado certificado = CertificadoService.certificadoPfxBytes(bytes, senha);

            ConfiguracoesNfe config = ConfiguracoesNfe.criarConfiguracoes(EstadosEnum.SP, AmbienteEnum.HOMOLOGACAO, certificado, "");

            //Efetua Consulta
            TRetConsStatServ retorno = Nfe.statusServico(config, DocumentoEnum.NFCE);

            //Resultado
            System.out.println();
            System.out.println("# Status: " + retorno.getCStat() + " - " + retorno.getXMotivo());
        } catch (CertificadoException | NfeException | IOException e) {
            System.err.println("# Erro: " + e.getMessage());
        }
    }

}
