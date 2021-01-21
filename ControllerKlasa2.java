package com.example.demo.contoller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.demo.repository.IzvodjenjeRepository;
import com.example.demo.repository.PredstavaRepository;
import com.example.demo.repository.ScenaRepository;
import com.example.demo.repository.UlogaRepository;

import model.Izvodjenje;
import model.Predstava;
import model.Scena;
import model.Uloga;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Controller
@RequestMapping(value="/controller2")
public class ControllerKlasa2 {

	@Autowired
	ScenaRepository sr;
	
	@Autowired
	IzvodjenjeRepository ir;
	
	@Autowired
	PredstavaRepository pr;
	
	@Autowired
	UlogaRepository ur;
	
	@RequestMapping(value="/getZanrovi", method=RequestMethod.GET)
	public String getScene(HttpServletRequest request) {
		List<Scena> scene = sr.findAll();
		
		request.getSession().removeAttribute("scene");
		request.getSession().removeAttribute("poruka");
		
		if ( scene != null) {
			request.getSession().setAttribute("scene", scene);
		} else {
			request.getSession().setAttribute("poruka", "Nema nijedne scene u bazi!");
		}
		return "prikaziScene";
	}
	
	@RequestMapping(value="/getIzvodjenja", method=RequestMethod.GET)
	public String vratiIzvodjenja(HttpServletRequest request) {
		Integer idIzv = Integer.parseInt(request.getParameter("scena"));
		Scena sc = sr.findById(idIzv).get();
		List<Izvodjenje> izvodjenja = ir.findByScena(sc);
		
		request.getSession().removeAttribute("izvodjenja");
		request.getSession().removeAttribute("poruka");
		
		if ( izvodjenja != null) {
			request.getSession().setAttribute("izvodjenja", izvodjenja);
		} else {
			request.getSession().setAttribute("poruka", "Nema izvodjenja na izabranoj sceni!");
		} 
		return "prikaziScene";
	}
	
	@RequestMapping(value="/getIzvestaj", method=RequestMethod.GET)
	public void generisiIzvestaj(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Integer idP = Integer.parseInt(request.getParameter("idP"));
		
		Predstava pred = pr.findById(idP).get();
		List<Uloga> uloge = ur.findByPredstava(pred);
		
		if (uloge == null) {
			request.getSession().setAttribute("poruka", "Nema uloga za ovu predstavu");
			return;
		}
		
		response.setContentType("text/html");
		JRBeanCollectionDataSource jrb = new JRBeanCollectionDataSource(uloge);
		InputStream in = this.getClass().getResourceAsStream("/reports/ulogeReport.jrxml");
		
		JasperReport jasperR = JasperCompileManager.compileReport(in);
		
		Map<String, Object> mapa = new HashMap<String, Object>();
		
		String predstavaNaziv = uloge.get(0).getPredstava().getNaziv();
		Integer predstavaTrajanje = uloge.get(0).getPredstava().getTrajanje();
		String predstavaOpis = uloge.get(0).getPredstava().getOpis();
		String predstavaZanr = uloge.get(0).getPredstava().getZanr().getNaziv();
		String predstavaReziser = uloge.get(0).getPredstava().getReziser().getIme()+" "+uloge.get(0).getPredstava().getReziser().getPrezime();
		
		if(uloge!=null && uloge.size()>0) {
			mapa.put("predNaziv", predstavaNaziv);
			mapa.put("predTrajanje", predstavaTrajanje);
			mapa.put("predOpis", predstavaOpis);
			mapa.put("predZanr", predstavaZanr);
			mapa.put("predReziser", predstavaReziser);
		}
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperR, mapa, jrb);
		in.close();
		
		response.setContentType("application/x-download");
		response.addHeader("Content-disposition", "attachment; filename=UlogePredstave.pdf");
		OutputStream out = response.getOutputStream();
		JasperExportManager.exportReportToPdfStream(jasperPrint,out);
	}
}
