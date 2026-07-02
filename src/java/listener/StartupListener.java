package listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import util.DBContext;
import util.MoviePosterService;

@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String webRoot = ctx.getRealPath("/");
        Connection conn = DBContext.getInstance().getConnection();
        MoviePosterService.ensurePosters(conn, webRoot);
    }
}
