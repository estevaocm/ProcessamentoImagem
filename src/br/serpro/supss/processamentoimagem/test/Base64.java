package br.serpro.supss.processamentoimagem.test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Base64 {

	public static void extractbase64() throws IOException{
		File arq = new File("imagens-ajustado.log");
		System.out.println(arq.exists());//true
		BufferedReader r = new BufferedReader(new FileReader(arq));
		ArrayList<String> linhas = new ArrayList<String>();
		try{
			String linha = r.readLine();
			while(linha != null && !linha.isEmpty()){
				linhas.add(linha);
				linha = r.readLine();
			}
		}
		finally{
			r.close();
		}
		System.out.println(linhas.size());//501
		int i=0;
		FileOutputStream out = null;
		for(String l : linhas){
			byte[] b = java.util.Base64.getDecoder().decode(l);
			i++;
			try{
				out = new FileOutputStream(new File("~/fotos/" + i + ".jpg"));
				out.write(b);
				out.flush();
			}
			finally{
				out.close();
			}
		}
	}
	
}
