package com.coworking.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.coworking.domain.Centre_coworking;
import com.coworking.domain.ModelException;
import com.coworking.domain.Usuari_registrat;
import com.coworking.interfaces.ICentre_coworkingDAO;
import com.coworking.interfaces.IUsuari_registratDAO;


/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	//Testeando que se pueden hacer COMMITS
		private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
		
		/**
		 * Simply selects the home view to render by returning its name.
		 */
		@Autowired
		private IUsuari_registratDAO Iusuari_registrat;
		@Autowired
		private ICentre_coworkingDAO Icentre_coworking;

		public Boolean loguejat = false;

		public String loginname;
		//usuario actual logeado
		public Usuari_registrat userlogged;
		//centro actual (solo se usa para edicion del centro)
		public Centre_coworking mycentre;

		private void afegeixDadesTopBar(ModelMap model) {
			model.put("loguejat", loguejat);
			if (loguejat){
				model.put("loginname", loginname);
				model.put("centresAdministrats", userlogged.getcentres_administrats());	
			}
		}
		
		@RequestMapping("/home")
		public ModelAndView getLoginForm(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				ModelMap model,
				BindingResult result) {
				System.out.println("loguejat = "+loguejat);

				//Si est� logueado pasamos datos para la top navbar
				if (loguejat) {
					//actualizamos userlogged porque se ve que si haces un registro de un centro va a null pointer
					userlogged = Iusuari_registrat.getUsuari_registrat(userlogged.getemail(), userlogged.getcontrasenya());
					afegeixDadesTopBar(model);
				}
				
				//A�adimos los centros para el display del home
				List<Centre_coworking> millorValorats = Icentre_coworking.getCentres();
				model.put("millorValorats", millorValorats);
				
				return new ModelAndView("home", "model", model);
		}
		

		@RequestMapping(value = "/login")
		public @ResponseBody /*JsonResponse*/String login(@RequestBody Usuari_registrat usuari_registrat,
				ModelMap model,
				BindingResult result) throws ModelException {
			
				JsonResponse res = new JsonResponse();
			
				if (result.hasErrors()) {
					logger.info("Logging fails: form validation errors");
					//return "home";
				}
			
				String email = usuari_registrat.getemail();
				String contrasenya = usuari_registrat.getcontrasenya();
			
				System.out.println("LOGIN =" + email);
				System.out.println("CONTRASENYA =" + contrasenya);
				
				//String retorn = "redirect:/";
				
				userlogged = Iusuari_registrat.getUsuari_registrat(email, contrasenya);
				
				if(userlogged == null){
					System.out.println("Usuario erroneo");
					result.rejectValue("", "wrongValue", "Usuari o Contrasenya Incorrecte");
					loguejat = false;
					loginname = null;
					throw new ModelException("Usuario no correcto"); 
				}else{
					System.out.println("Usuario correcto");
					loguejat = true;
					loginname = userlogged.getemail();
					
					afegeixDadesTopBar(model);
					
					res.setStatus("CORRECTE");
					res.setResult(model);
				}
				return "{\"status\":\"CORRECTE\", \"result\": {\"loguejat\":true, \"loginname\":\"test\"}}";
		}

		
		/* ---------- Actualitza llista Els Meus Espais // Inutil, carrega per login/home ------------- */
		@RequestMapping(value = "/espais", method = RequestMethod.GET)
		@ResponseBody
		public List<Centre_coworking> getCentresAdministrats() {
			System.out.println("Chivato: He clickat a a Els Meus Espais, generant una URL /home/espais que hem retorna a la funci� AJAX una llista de Centres");
			return userlogged.getcentres_administrats();
		}
		
		@RequestMapping(value = "/logout")
		public ModelAndView logout() {
			
				logger.info("Logging out user with username = "+ userlogged);
			
				userlogged = null;
				loguejat = false;
				loginname = null;
				mycentre = null;
				return new ModelAndView("redirect:/");
		}
		
		@RequestMapping("/register")
		public ModelAndView getRegisterForm(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result) {
			usuari_registrat = new Usuari_registrat();
			ArrayList<String> ambit = new ArrayList<String>();
			ambit.add("Arquitecte");
			ambit.add("Enginyer de software");
			ambit.add("Matem�tic");
			ambit.add("Enginyer electronic");
			ArrayList<String> privacitat = new ArrayList<String>();
			privacitat.add("SI");
			privacitat.add("NO");
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("ambit", ambit);
			model.put("privacitat", privacitat);
			
			System.out.println("Register Form");
			return new ModelAndView("Register", "model", model);
		}

		@RequestMapping("/saveUser")
		public ModelAndView saveUserData(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result) {
			usuari_registrat.setadmin_centre(false);
			usuari_registrat.setactiu(true);
			usuari_registrat.setdata_caducitat(null);
			Boolean correcte = true;
			//si no funciona en vez del model ponerlo en una global
			Map<String, Object> model = new HashMap<String, Object>();
			if(usuari_registrat.getnom().isEmpty()){
				model.put("error", "El nom no pot estar buit");
				correcte = false;
			}else if(usuari_registrat.getcognom().isEmpty()){
				model.put("error", "El cognom no pot estar buit");
				correcte = false;
			}else if(usuari_registrat.getemail().isEmpty()){
				model.put("error", "L'email no pot estar buit");
				correcte = false;
			}else if(usuari_registrat.getcontrasenya().isEmpty()){
				model.put("error", "La contrasenya no pot estar buida");
				correcte = false;
			}else if(usuari_registrat.getdni().isEmpty()){
				model.put("error", "El dni no pot estar buit");
				correcte = false;
			}
		
			try {
				if(correcte == false){
					return this.getRegisterForm(usuari_registrat, result);
				}
				Iusuari_registrat.saveUsuari_registrat(usuari_registrat);
				System.out.println("Save usuari_registrat");
				return new ModelAndView("redirect:/home.html");
			}catch(Exception e){
				System.out.println(e.getMessage());
				return this.getRegisterForm(usuari_registrat, result);
			}
			
			
		}
		@RequestMapping("/updateUser")
		public ModelAndView updateUserData(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result, ModelMap model) {
			Boolean correcte = true;
			//si no funciona en vez del model ponerlo en una global
			try {
				
				if(usuari_registrat.getnom().isEmpty()){
					model.put("error", "El nom no pot estar buit");
					correcte = false;
				}else if(usuari_registrat.getcognom().isEmpty()){
					model.put("error", "El cognom no pot estar buit");
					correcte = false;
				}else if(usuari_registrat.getemail().isEmpty()){
					model.put("error", "L'email no pot estar buit");
					correcte = false;
				}else if(usuari_registrat.getcontrasenya().isEmpty()){
					model.put("error", "La contrasenya no pot estar buida");
					correcte = false;
				}else if(usuari_registrat.getdni().isEmpty()){
					model.put("error", "El dni no pot estar buit");
					correcte = false;
				}
				if(correcte==false){
					return this.editprofile(usuari_registrat, result, model);
				}
				Iusuari_registrat.updateUsuari_registrat(usuari_registrat);
				System.out.println("Update usuari_registrat");
				//actualizar el userlogged para reflejar cambios
				userlogged = Iusuari_registrat.getUsuari_registrat(userlogged.getemail(), userlogged.getcontrasenya());
				return new ModelAndView("redirect:/myprofile.html");
			}catch(Exception e){
				System.out.println(e.getMessage());
				return this.editprofile(usuari_registrat, result, model);
			}
			
			
		}
		
		@RequestMapping("/updateCentre")
		public ModelAndView updateCentre(@ModelAttribute("centre_coworking") Centre_coworking centre_coworking,
				BindingResult result, ModelMap model) {
			Boolean correcte = true;
			
			try {
				if(centre_coworking.getNom().isEmpty()){
					model.put("error", "El nom no pot estar buit");
					correcte = false;
				}else if(centre_coworking.getDescripcio().isEmpty()){
					model.put("error", "La descripcio no pot estar buida");
					correcte = false;
				}else if(centre_coworking.getEmail().isEmpty()){
					model.put("error", "L'email no pot estar buit");
					correcte = false;
				}
				if(correcte==false){
					return this.editcenter(userlogged, centre_coworking, result, model);
				}
				
				centre_coworking.setAdmin_centre(mycentre.getAdmin_centre());
				Icentre_coworking.updateCentre(centre_coworking);
				System.out.println("Update centre");
				return new ModelAndView("redirect:/mycenterprofile.html?centreId="+centre_coworking.getIdcentre());
			}catch(Exception e){
				System.out.println(e.getMessage());
				return this.editcenter(userlogged, centre_coworking, result, model);
			}
			
			
		}

		@RequestMapping("/registerCentre")
		public ModelAndView getRegisterCentre(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat, 
				@ModelAttribute("centre_coworking") Centre_coworking centre_coworking,
				ModelMap model,
				BindingResult result) {
			ArrayList<String> poblacions = new ArrayList<String>();
			poblacions.add("Barcelona");
			poblacions.add("Madrid");
			poblacions.add("Sevilla");
			poblacions.add("Galicia");
			
			afegeixDadesTopBar(model);
			model.put("poblacio", poblacions);
			
			return new ModelAndView("RegisterCentre", "model", model);
		}

		@RequestMapping("/saveCentre")
		public ModelAndView saveCentreData(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				@ModelAttribute("centre_coworking") Centre_coworking centre_coworking,
			ModelMap model,
			BindingResult result) {
			Boolean correcte = true;
			ArrayList<String> poblacions = new ArrayList<String>();
			poblacions.add("Barcelona");
			poblacions.add("Madrid");
			poblacions.add("Sevilla");
			poblacions.add("Galicia");
			model.put("poblacio", poblacions);
			try {
				if(centre_coworking.getNom().isEmpty()){
					model.put("error", "El nom no pot estar buit");
					correcte = false;
				}else if(centre_coworking.getDescripcio().isEmpty()){
					model.put("error", "La descripcio no pot estar buida");
					correcte = false;
				}else if(centre_coworking.getEmail().isEmpty()){
					model.put("error", "L'email no pot estar buit");
					correcte = false;
				}
				if(correcte==false){
					return this.editcenter(userlogged, centre_coworking, result, model);
				}
				centre_coworking.setAdmin_centre(userlogged);
				System.out.println("LIIIIIIIIIINK= "+centre_coworking.getlink_foto());
				Icentre_coworking.saveCentre_coworking(centre_coworking);
				userlogged.addcentre_administrat(centre_coworking);
				System.out.println("Save centre_coworking");
				return new ModelAndView("redirect:/home.html");
			}catch(Exception e){
				System.out.println(e.getMessage());
				return this.getRegisterCentre(null, centre_coworking, model, result);
			}
			
		}
		
		@RequestMapping("/userList")
		public ModelAndView getUserList(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat, ModelMap model) {
			
			afegeixDadesTopBar(model);
			model.put("llistat_usuaris", Iusuari_registrat.getUsuaris());

			System.out.println("SIZE= "+Iusuari_registrat.getUsuaris().size());
			return new ModelAndView("UserDetails", "model", model );

		}
		
		@RequestMapping("/centresList")
		public ModelAndView getCentresDetais(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result, ModelMap model) {
			
			afegeixDadesTopBar(model);
			model.put("centre_coworking", Icentre_coworking.getCentres());

			return new ModelAndView("CentresDetails", "model", model);

		}
		@RequestMapping("/mycentres")
		public ModelAndView getmycentres(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("centre_coworking", Icentre_coworking.getCentres());
			model.put("loguejat", loguejat);
			model.put("loginname", loginname);
			usuari_registrat = userlogged;
			if(!usuari_registrat.getcentres_administrats().isEmpty()){
				model.put("centres_admin", usuari_registrat.getcentres_administrats());	
			}else{
				System.out.println("este usuario no administra ningun centro");
			}
			return new ModelAndView("mycentres", "model", model);

		}
		@RequestMapping("/myprofile")
		public ModelAndView getmyprofile(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result, ModelMap model) {
			usuari_registrat = userlogged;

			afegeixDadesTopBar(model);
			
			model.put("email", usuari_registrat.getemail());
			model.put("nom", usuari_registrat.getnom());
			model.put("cognom", usuari_registrat.getcognom());
			model.put("ambit", usuari_registrat.getamb_prof());
			model.put("adreca", usuari_registrat.getadreca());
			if(usuari_registrat.getlink_foto()==null){
				model.put("link", "http://www.freelancer.com.es/img/unknown.png");
			}else{
				if(usuari_registrat.getlink_foto().isEmpty()){
					
					model.put("link", "http://www.freelancer.com.es/img/unknown.png");
				}else{
					model.put("link", usuari_registrat.getlink_foto());
				}
			}
		
			model.put("dni", usuari_registrat.getdni());
			model.put("data_naix", usuari_registrat.getdata_naix()+"");
			model.put("telefon", usuari_registrat.gettelefon());
			model.put("privacitat", usuari_registrat.getprivacitat());
			model.put("sobre_mi", usuari_registrat.getsobre_mi());
			model.put("web", usuari_registrat.getweb());
			model.put("premium", usuari_registrat.getpremium());
		
							
			return new ModelAndView("myprofile", "model", model);

		}
		@RequestMapping("/editprofile")
		public ModelAndView editprofile(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				BindingResult result, ModelMap model) {
			usuari_registrat = userlogged;
			ArrayList<String> ambit = new ArrayList<String>();
			ambit.add("Arquitecte");
			ambit.add("Enginyer de software");
			ambit.add("Matematic");
			ambit.add("Enginyer electronic");
			
			//select boxes para privacitat y premium
			ArrayList<String> boxpriv = new ArrayList<String>();
			ArrayList<String> boxprem = new ArrayList<String>();

			afegeixDadesTopBar(model);
			
			model.put("email", usuari_registrat.getemail());
			model.put("nom", usuari_registrat.getnom());
			model.put("cognom", usuari_registrat.getcognom());
			model.put("ambit", ambit);
			model.put("dni", usuari_registrat.getdni());
			model.put("data_naix", usuari_registrat.getdata_naix()+"");
			model.put("telefon", usuari_registrat.gettelefon());
			model.put("contrasenya", usuari_registrat.getcontrasenya());
			model.put("adreca", usuari_registrat.getadreca());
			model.put("privacitat", usuari_registrat.getprivacitat());
			model.put("actiu", usuari_registrat.getactiu());
			model.put("admin", usuari_registrat.getadmin_centre());
			model.put("data_cad", usuari_registrat.getdata_caducitat());
			model.put("link", usuari_registrat.getlink_foto());
			if(usuari_registrat.getprivacitat().matches("SI")){
				boxpriv.add("NO");
				model.put("boxpriv", boxpriv);
			}else{
				boxpriv.add("SI");
				model.put("boxpriv", boxpriv);
			}
			
			model.put("sobre_mi", usuari_registrat.getsobre_mi());
			model.put("web", usuari_registrat.getweb());
			model.put("premium", usuari_registrat.getpremium());
							
			if(usuari_registrat.getpremium().matches("SI")){
				boxprem.add("NO");
				model.put("boxprem", boxprem);
			}else{
				boxprem.add("SI");
				model.put("boxprem", boxprem);
			}
			return new ModelAndView("editprofile", "model", model);

		}
		
		@RequestMapping(value="mycenterprofile", method=RequestMethod.GET)
		public ModelAndView mycenterprofile(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				@RequestParam(value="centreId", required=true) Integer centreId, HttpServletRequest request,  
	            HttpServletResponse response, ModelMap model) {       
	        
	        System.out.println("Got request param: " + centreId);

			Centre_coworking centre=Icentre_coworking.getCentre_coworking(centreId);
			Boolean autoritzat = false;
			
			afegeixDadesTopBar(model);
			if(loguejat){
				List<Centre_coworking> llistacentres = userlogged.getcentres_administrats();
				if(llistacentres.contains(centre)){
					System.out.println("PERMISOS CORRECTOS");
					autoritzat = true;
					model.put("nom", centre.getNom());
					model.put("descripcio", centre.getDescripcio());
					model.put("email", centre.getEmail());
					model.put("telefon", centre.getTelefon());
					model.put("web", centre.getWeb());
					model.put("carrer", centre.getCarrer());
					model.put("poblacio", centre.getpoblacio());
					model.put("num_edifici", centre.getnum_edifici());
					model.put("capacitat", centre.getcapacitat());
					if(centre.getlink_foto()==null){
						model.put("link", "http://4vector.com/i/free-vector-buildings-icon_101963_Buildings_icon.png");
					}else{
						if(centre.getlink_foto().isEmpty()){
							model.put("link", "http://4vector.com/i/free-vector-buildings-icon_101963_Buildings_icon.png");
						}else{
							model.put("link", centre.getlink_foto());
						}
					}
					model.put("num_edifici", centre.getnum_edifici());
					
					//para saber que el centro que vas a editar es tuyo
					//por seguridad, sino se usaria el request param como en el perfil
					mycentre = centre;
				}
				model.put("autoritzat", autoritzat);
			}
					
			return new ModelAndView("mycenterprofile", "model", model);
			
		}
		@RequestMapping(value="editcenter"/*, method=RequestMethod.GET*/)
		public ModelAndView editcenter(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
				@ModelAttribute("centre_coworking") Centre_coworking centre_coworking,
				BindingResult result, ModelMap model) {       
			
			afegeixDadesTopBar(model);
			
	        if(mycentre!=null){
	        	ArrayList<String> poblacions = new ArrayList<String>();
	    		poblacions.add("Barcelona");
	    		poblacions.add("Madrid");
	    		poblacions.add("Sevilla");
	    		poblacions.add("Galicia");
	    		model.put("poblacio", poblacions);
				model.put("nom", mycentre.getNom());
				model.put("descripcio", mycentre.getDescripcio());
				model.put("email", mycentre.getEmail());
				model.put("telefon", mycentre.getTelefon());
				model.put("web", mycentre.getWeb());
				model.put("idcentre", mycentre.getIdcentre());
				model.put("admin_centre", mycentre.getAdmin_centre());
				model.put("carrer", mycentre.getCarrer());
				model.put("link", mycentre.getlink_foto());
				model.put("capacitat", mycentre.getcapacitat());
				model.put("num_edifici", mycentre.getnum_edifici());
				if(mycentre.getBanys()){
					model.put("banys", "checked");
				}
				if(mycentre.getCafeteria()){
					model.put("cafeteria", "checked");
				}
				if(mycentre.getSala_reunions()){
					model.put("sala", "checked");
				}
				if(mycentre.getInternet()){
					model.put("internet", "checked");
				}
	        }
							
			return new ModelAndView("editcenter", "model", model);
		}
		
		@RequestMapping(value="centerprofile", method=RequestMethod.GET)
		public ModelAndView centerprofile(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat, 
				@RequestParam(value="centreId", required=true) Integer centreId, HttpServletRequest request,  
	            HttpServletResponse response, ModelMap model) {       
	        
	        System.out.println("Got request param: " + centreId);

			Centre_coworking centre=Icentre_coworking.getCentre_coworking(centreId);
			
			afegeixDadesTopBar(model);
			
			model.put("nom", centre.getNom());
			model.put("descripcio", centre.getDescripcio());
			model.put("email", centre.getEmail());
			model.put("telefon", centre.getTelefon());
			model.put("web", centre.getWeb());
			model.put("carrer", centre.getCarrer());
			model.put("poblacio", centre.getpoblacio());
			model.put("num_edifici", centre.getnum_edifici());
			model.put("capacitat", centre.getcapacitat());
			String concat = centre.getpoblacio().concat(" ").concat(centre.getCarrer());
			model.put("maps", concat);
			if(centre.getvaloracio() == 0){
				model.put("valoracio", "No t� cap valoraci�");
			}else{
				model.put("valoracio", centre.getvaloracio());
			}
			if(centre.getlink_foto()==null){
				model.put("link", "http://4vector.com/i/free-vector-buildings-icon_101963_Buildings_icon.png");
			}else{
				if(centre.getlink_foto().isEmpty()){
					model.put("link", "http://4vector.com/i/free-vector-buildings-icon_101963_Buildings_icon.png");
				}else{
					model.put("link", centre.getlink_foto());
				}
			}
			model.put("num_edifici", centre.getnum_edifici());
			
					
			return new ModelAndView("centerprofile", "model", model);
			
		}
		
	@RequestMapping(value="userprofile", method=RequestMethod.GET)
	public ModelAndView userprofile(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat,
			@RequestParam(value="userId", required=true) Integer userId, HttpServletRequest request,  
            HttpServletResponse response, ModelMap model) {   
		
		Usuari_registrat usuari=Iusuari_registrat.getusuari_registrat(userId);

		afegeixDadesTopBar(model);
		
		model.put("email", usuari.getemail());
		model.put("nom", usuari.getnom());
		model.put("cognom", usuari.getcognom());
		model.put("ambit", usuari.getamb_prof());
		model.put("adreca", usuari.getadreca());
		if(usuari.getlink_foto()==null){
			model.put("link", "http://www.freelancer.com.es/img/unknown.png");
		}else{
			if(usuari.getlink_foto().isEmpty()){
				
				model.put("link", "http://www.freelancer.com.es/img/unknown.png");
			}else{
				model.put("link", usuari.getlink_foto());
			}
		}
	
		model.put("dni", usuari.getdni());
		model.put("data_naix", usuari.getdata_naix()+"");
		model.put("telefon", usuari.gettelefon());
		model.put("privacitat", usuari.getprivacitat());
		model.put("sobre_mi", usuari.getsobre_mi());
		model.put("web", usuari.getweb());
		model.put("premium", usuari.getpremium());
	
					
		return new ModelAndView("userprofile", "model", model);

	}
	
	/*@RequestMapping("/cercaAvancada")
	public ModelAndView cercaAvancada(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat, 
			@ModelAttribute("centre_coworking") Centre_coworking centre_coworking,
			ModelMap model,
			BindingResult result) {
		
		afegeixDadesTopBar(model);
		
		return new ModelAndView("cercaAvancada", "model", model);
	}
	
	@RequestMapping("/cercaAvancadaResultats")
	public ModelAndView cercaAvancadaResultats(@ModelAttribute("usuari_registrat") Usuari_registrat usuari_registrat, 
			@ModelAttribute("centre_coworking") Centre_coworking centre_coworking,
			ModelMap model,
			BindingResult result) {
		
		afegeixDadesTopBar(model);
		
		return new ModelAndView("cercaAvancada", "model", model);
	}
	*/
}
