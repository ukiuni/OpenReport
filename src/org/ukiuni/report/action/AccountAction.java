package org.ukiuni.report.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.util.Base64;

import org.apache.commons.beanutils.BeanUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.ukiuni.report.ResponseServerStatusException;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.AccountAccessKey;
import org.ukiuni.report.entity.IconImage;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.service.IconImageService;

@Path("account")
public class AccountAction {
	private static final long IMAGE_ICON_SOURCE_MAX_SIZE = 10000000;
	public AccountService accountService = new AccountService();
	public IconImageService iconImageService = new IconImageService();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AccountDto create(AccountDto saveAccount) throws IllegalAccessException, InvocationTargetException, NoSuchAlgorithmException, UnsupportedEncodingException {
		if (accountService.existsName(saveAccount.getName())) {
			throw new ResponseServerStatusException(409, "name");
		}
		if (accountService.existsMail(saveAccount.getMail())) {
			throw new ResponseServerStatusException(409, "email");
		}
		Account account = new Account();
		BeanUtils.copyProperties(account, saveAccount);
		account.setPasswordHashed(Base64.encode(MessageDigest.getInstance("SHA1").digest(saveAccount.getPassword().getBytes("UTF-8"))));
		accountService.save(account);

		AccountDto returnAccount = new AccountDto();
		BeanUtils.copyProperties(returnAccount, account);
		return returnAccount;
	}

	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AccountDto login(AccountDto saveAccount) throws IllegalAccessException, InvocationTargetException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Account account = accountService.findByName(saveAccount.name);
		if (null == account || !account.getPasswordHashed().equals(Base64.encode(MessageDigest.getInstance("SHA1").digest(saveAccount.getPassword().getBytes("UTF-8"))))) {
			throw new ResponseServerStatusException(400, "name or password not match");
		}
		AccountAccessKey accountAccessKey = accountService.generateAccessKey(account);
		AccountDto returnAccount = new AccountDto();
		BeanUtils.copyProperties(returnAccount, account);
		returnAccount.setAccessKey(accountAccessKey.getHash());
		return returnAccount;
	}

	@GET
	@Path("loadByAccessKey")
	public AccountDto loadByAccessKey(@QueryParam("accessKey") String accessKey) throws IllegalAccessException, InvocationTargetException {
		Account account = accountService.findByAccessKey(accessKey);
		AccountDto returnAccount = new AccountDto();
		BeanUtils.copyProperties(returnAccount, account);
		return returnAccount;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update")
	public AccountDto update(String body) throws IllegalAccessException, InvocationTargetException {
		System.out.println("update " + body);
		AccountDto saveAccount = JSON.decode(body, AccountDto.class);
		Account targetAccount = accountService.findByAccessKey(saveAccount.getAccessKey());
		if (targetAccount == null) {
			throw new NotFoundException("account not found");
		}
		BeanUtils.copyProperties(targetAccount, saveAccount);
		System.out.println("save   Account = " + saveAccount);
		System.out.println("target Account = " + targetAccount);
		accountService.update(targetAccount);
		BeanUtils.copyProperties(saveAccount, targetAccount);
		return saveAccount;
	}

	@POST
	@Path("/registImage")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String regist(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileDisposition, @FormDataParam("accountAccessKey") String accountAccessKey) throws IOException {
		if (fileDisposition.getSize() > IMAGE_ICON_SOURCE_MAX_SIZE) {
			throw new BadRequestException("image is too large");
		}
		Account account = accountService.findByAccessKey(accountAccessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		IconImage iconImage = iconImageService.regist(account, file);
		return iconImage.getKey();
	}

	@GET
	@Path("/icon/{imageKey}")
	@Produces("image/png")
	public Response icon(@PathParam("imageKey") final String imageKey) {
		final IconImage iconImage = iconImageService.loadByKey(imageKey);
		if (null == iconImage) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok().entity(new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				output.write(iconImage.getContent());
				output.flush();
			}
		}).build();
	}

	@SuppressWarnings("serial")
	public static class AccountDto implements Serializable {
		@Size(min = 6, max = 20, message = "An event's name must contain between 6 and 1000 characters")
		// TODO JSONだとvalidationが効かない
		private String name;
		@NotNull
		private String mail;
		@NotNull
		private String password;
		@NotNull
		private String profile;
		private String iconUrl;
		private String accessKey;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getProfile() {
			return profile;
		}

		public void setProfile(String profile) {
			this.profile = profile;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}

		@Override
		public String toString() {
			return "SaveAccount [name=" + name + ", mail=" + mail + ", password=" + password + ", profile=" + profile + ", iconUrl=" + iconUrl + "]";
		}

		public String getAccessKey() {
			return accessKey;
		}

		public void setAccessKey(String accessKey) {
			this.accessKey = accessKey;
		}
	}
}