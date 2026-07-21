package controller;

import dao.ManagerPerformanceDAO;
import dto.ManagerRevenueSummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import model.User;

/** Chi tiết doanh thu riêng của manager, không gọi bất kỳ báo cáo Admin nào. */
@WebServlet("/manager/revenue")
public class ManagerRevenueController extends HttpServlet {
    private static final int PAGE_SIZE=20;
    private final ManagerPerformanceDAO dao=new ManagerPerformanceDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        HttpSession session=req.getSession(false); User user=session==null?null:(User)session.getAttribute("user");
        if(user==null || !"MANAGER".equalsIgnoreCase(user.getRole())){res.sendRedirect(req.getContextPath()+"/home");return;}
        int branchId=dao.getAssignedBranchId(user.getId()); if(branchId<=0){res.sendError(HttpServletResponse.SC_FORBIDDEN,"Manager chưa được gán chi nhánh.");return;}
        LocalDate from=parse(req.getParameter("fromDate")), to=parse(req.getParameter("toDate"));
        if(from!=null&&to!=null&&to.isBefore(from))to=from; if(from!=null&&to==null)to=from; if(from==null&&to!=null)from=to;
        Timestamp start=from==null?null:Timestamp.valueOf(from.atStartOfDay()), end=to==null?null:Timestamp.valueOf(to.plusDays(1).atStartOfDay());
        int total=dao.countRevenueTransactions(branchId,start,end); int pages=Math.max(1,(int)Math.ceil(total/(double)PAGE_SIZE)); int page=parsePage(req.getParameter("page"),pages);
        req.setAttribute("revenue",dao.getRevenueSummary(branchId,start,end)); req.setAttribute("transactions",dao.getRevenueTransactions(branchId,start,end,(page-1)*PAGE_SIZE,PAGE_SIZE));
        req.setAttribute("fromDate",from==null?"":from.toString());req.setAttribute("toDate",to==null?"":to.toString());req.setAttribute("page",page);req.setAttribute("totalPages",pages);req.setAttribute("total",total);
        req.getRequestDispatcher("/pages/manager/revenue-detail.jsp").forward(req,res);
    }
    private LocalDate parse(String v){try{return v==null||v.isBlank()?null:LocalDate.parse(v);}catch(Exception e){return null;}}
    private int parsePage(String v,int max){try{return Math.max(1,Math.min(Integer.parseInt(v),max));}catch(Exception e){return 1;}}
}
