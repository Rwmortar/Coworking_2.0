package com.coworking.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "solicitud_lloc")
public class Solicitud_lloc {
	
	@Id
	@Column(name = "idsolicitud")
	private int idsolicitud;
	
	@Column(name = "idlloc")
	private int idlloc;
	
	@Column(name = "acceptada")
	private boolean acceptada;

	@Column(name = "admin")
	private String admin;
	
	@Column(name = "usuari")
	private String usuari;

	public int getIdsolicitud() {
		return idsolicitud;
	}

	public void setIdsolicitud(int idsolicitud) {
		this.idsolicitud = idsolicitud;
	}

	public int getIdlloc() {
		return idlloc;
	}

	public void setIdlloc(int idlloc) {
		this.idlloc = idlloc;
	}

	public boolean isAcceptada() {
		return acceptada;
	}

	public void setAcceptada(boolean acceptada) {
		this.acceptada = acceptada;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getUsuari() {
		return usuari;
	}

	public void setUsuari(String usuari) {
		this.usuari = usuari;
	}
}