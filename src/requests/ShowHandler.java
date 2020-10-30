package requests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import models.Root;
import models.Show;

// Singleton class
public class ShowHandler extends Handler<Root,Show> {
	
	private ShowHandler() {
		setUriPrefix("/watch/");
	}
	
	private static ShowHandler instance = null;
		
	public static ShowHandler getInstance() {
		if(instance == null) {
			instance = new ShowHandler();
		}
		return instance;
	}
	
	public BufferedImage getShowImage(int ShowID) throws InterruptedException, IOException {
		File targetFile = new File(String.format("%s/%s.jpg", conf.IMAGES_PATH, ShowID));
		BufferedImage showImage;
		if (targetFile.exists()) {
			showImage = ImageIO.read(targetFile);
		} else {
			// if image not available getting it from website
			showImage = ImageIO.read(new URL(String.format("%s/series/%s.jpg", conf.getStaticSdarotURI(), ShowID)));
			targetFile.getParentFile().mkdirs();
			ImageIO.write(showImage, "jpg", targetFile);
		}
		return showImage;
	}
	
	private String getShowName(Root r, int ShowID) {
		String name = String.format("%s", ShowID); 
		try {
			HttpResponse<String> response = ShowHandler.getInstance().getPageResponse(r, ShowID);
			Pattern showNameVar = Pattern.compile("(var Sname.*=.*)");
			Matcher m = showNameVar.matcher(response.body());
			if (m.find()) {
				name = m.group().split("=")[1].split(",")[1].replace("\"", "").replace("[","").replace("]", "");
			} else {
				System.out.println("could not get show name... setting the show name as the id");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
	}
	
	@Override
	public List<Show> getAll(Root root) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Can't get all shows at once!");
	}
	
	@Override
	protected HttpResponse<String> getFatherPageResponse(Root root) throws IOException, InterruptedException {
		throw new UnsupportedOperationException("This function is not implemented yet");
	}
	
	@Override
	public Pattern getPattern(Root father) {
		throw new UnsupportedOperationException("This function is not implemented yet");
	}
	
	@Override
	public String getSuffixUrl(Root root, int showID) {
		return String.format("%s%s", getUriPrefix(), showID);
	}

	@Override
	public Show getByID(Root root, int showID) throws IOException, InterruptedException {
		if(!IsExists(root, showID)) {
			return null;
		}
		Show ret = new Show(root, showID);
		SeasonHandler.getInstance().getAll(ret).forEach(s -> ret.AddChildren(s));
		ret.SetName(getShowName(root, showID));
		System.out.println(ret.getName());
		return ret;
	}
	
	@Override
	public void download(Show show) {
		show.getChildren().forEach((i, s) -> SeasonHandler.getInstance().download(s));
	}

}
