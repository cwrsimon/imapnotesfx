package de.wesim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import de.wesim.imapnotes.services.IMAPUtils;

public class TiffTest {

	public static void main(String[] args) throws IOException {
		byte[] tiffImage = Files.readAllBytes(Paths.get("/home/christian/tmp/test.tiff"));
		IMAPUtils utils = new IMAPUtils();
		byte[] pngImage = utils.convertTIFF2Jpeg(tiffImage);
		//Files.write(Paths.get("/home/christian/tmp/test.png"), pngImage);
		String base64 = Base64.getEncoder().encodeToString(pngImage);
		Files.write(Paths.get("/home/christian/tmp/test.base6"), List.of(base64));
	}

}
