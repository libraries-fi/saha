package fi.seco.saha3.web.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.google.common.io.Files;
import java.io.File;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Set;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

@Controller
public class ImageUploadController {
	@Autowired
	private SahaProjectRegistry registry;

	@Value("#{WWWDomain}")
	private String www_domain;

	@RequestMapping(
		method = RequestMethod.POST,
		value = "/service/pics_upload"
	)
	public String upload(
		RedirectAttributes redirAttr,
		@RequestParam("image") MultipartFile file,
		@RequestParam("target") String target,
		@RequestParam("model") String model,
		@RequestParam("property") String property) throws Exception {

		if (file.isEmpty()) {
			return "Invalid file";
		}

		// Compute hash of image content and use it as filename.
		// Copy file extension to new name.
		byte[] image_byte_hash = MessageDigest
			.getInstance("SHA-256")
			.digest(file.getBytes());
		String image_hash = Base64
			.getUrlEncoder()
			.withoutPadding()
			.encodeToString(image_byte_hash);
		String name = image_hash + '.' + Files.getFileExtension(file.getOriginalFilename());

		File picFile = new File(registry.getProjectBaseDirectory() + model + "/pics/" + name);
		file.transferTo(picFile);

		Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-r--r--");
		java.nio.file.Files.setPosixFilePermissions(picFile.toPath(), permissions);

		String image_url = this.www_domain + "images/" + model + '/' + name;

		redirAttr.addAttribute("uri", target);
		redirAttr.addAttribute("image_url", image_url);
		redirAttr.addAttribute("target", target);
		redirAttr.addAttribute("property", property);
		redirAttr.addAttribute("model", model);

		return "redirect:/project/editor.shtml";
	}
}
