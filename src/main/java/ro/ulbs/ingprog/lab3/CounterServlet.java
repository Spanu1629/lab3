package ro.ulbs.ingprog.lab3;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "counterServlet", value = "/CounterServlet")
public class CounterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        updateCounter(request, response, "CounterGetName");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        updateCounter(request, response, "CounterPostName");
    }

    private void updateCounter(HttpServletRequest request, HttpServletResponse response,
                               String counterName) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (request.getParameter("inc") == null || request.getParameter("dec") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Trimiteti formularul cu ambii parametri: inc si dec.");
            return;
        }
        HttpSession session = request.getSession();
        synchronized (session) {
            try {
                int increment = parameterValue(request.getParameter("inc"));
                if (increment < 0) {
                    throw new NumberFormatException("Incrementul nu poate fi negativ.");
                }
                int decrement = parameterValue(request.getParameter("dec"));
                int counter = counterValue(session, counterName);
                int result = Math.toIntExact((long) counter + increment - decrement);
                session.setAttribute(counterName, result);
            } catch (NumberFormatException | ArithmeticException exception) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("counterError",
                        "Introduceti numere intregi, cu inc >= 0; rezultatul trebuie sa fie intre -2147483648 si 2147483647.");
            }
            status(request, response);
        }
    }

    private int parameterValue(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
    }

    private int counterValue(HttpSession session, String counterName) {
        Integer counter = (Integer) session.getAttribute(counterName);
        return counter == null ? 0 : counter;
    }

    private void updateWarning(HttpSession session, String warningName, int counter) {
        if (counter == 0) {
            session.setAttribute(warningName, Boolean.TRUE);
        } else {
            session.removeAttribute(warningName);
        }
    }

    private void status(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        int counterGet = counterValue(session, "CounterGetName");
        int counterPost = counterValue(session, "CounterPostName");
        session.setAttribute("CounterGetName", counterGet);
        session.setAttribute("CounterPostName", counterPost);
        updateWarning(session, "WarningGetName", counterGet);
        updateWarning(session, "WarningPostName", counterPost);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println("<!DOCTYPE html><html lang=\"ro\"><head><meta charset=\"UTF-8\">");
        printWriter.println("<title>Counter Servlet</title></head><body>");
        printWriter.println("<h2>Welcome to Counter Servlet</h2>");
        if (request.getAttribute("counterError") != null) {
            printWriter.println("<p role=\"alert\">" + request.getAttribute("counterError") + "</p>");
        }
        printWriter.println("<p>Counter for GET: " + counterGet + "</p>");
        printWriter.println("<p>Counter for POST: " + counterPost + "</p>");
        if (Boolean.TRUE.equals(session.getAttribute("WarningGetName"))) {
            printWriter.println("<p>WARNING GET</p>");
        }
        if (Boolean.TRUE.equals(session.getAttribute("WarningPostName"))) {
            printWriter.println("<p>WARNING POST</p>");
        }
        printWriter.println("<p><a href=\"" + request.getContextPath() + "/index.jsp\">Inapoi la formular</a></p>");
        printWriter.println("</body></html>");
    }
}
