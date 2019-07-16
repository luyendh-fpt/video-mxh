package mxh.api;

import com.google.gson.Gson;
import mxh.entity.JsonResponse;
import mxh.entity.Video;
import mxh.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class VideoApi extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(VideoApi.class.getName());
    private static Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String strId = req.getParameter("id");
        if (strId == null || strId.length() == 0) {
            // get list
            getList(req, resp);
        } else {
            // get detail
            getDetail(req, resp, strId);
        }
    }

    private void getDetail(HttpServletRequest req, HttpServletResponse resp, String strId) throws IOException {
        Video video = ofy().load().type(Video.class).id(Long.parseLong(strId)).now();
        if (video == null || video.isDeactive()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_NOT_FOUND)
                    .setMessage("Video is not found or has been deleted!")
                    .toJsonString());
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(new JsonResponse()
                .setStatus(HttpServletResponse.SC_OK)
                .setMessage("Success!")
                .setData(video)
                .toJsonString());
    }

    private void getList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(new JsonResponse()
                .setStatus(HttpServletResponse.SC_OK)
                .setMessage("Success!")
                .setData(ofy().load().type(Video.class).filter("status", 1).list())
                .toJsonString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String content = StringUtil.convertInputStreamToString(req.getInputStream());
            Video video = gson.fromJson(content, Video.class);
            ofy().save().entity(video).now();
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_CREATED)
                    .setMessage("Create video success!")
                    .setData(video).toJsonString());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, String.format("Can not create video. Error: %s", ex.getMessage()));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .setMessage("Can not create video!")
                    .toJsonString());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String strId = req.getParameter("id");
        if (strId == null || strId.length() == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_BAD_REQUEST)
                    .setMessage("Bad request!")
                    .toJsonString());
            return;
        }
        Video existVideo = ofy().load().type(Video.class).id(Long.parseLong(strId)).now();
        if (existVideo == null || existVideo.isDeactive()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_NOT_FOUND)
                    .setMessage("Video is not found or has been deleted!")
                    .toJsonString());
            return;
        }
        String content = StringUtil.convertInputStreamToString(req.getInputStream());
        Video updateVideo = gson.fromJson(content, Video.class);
        existVideo.setName(updateVideo.getName());
        existVideo.setDescription(updateVideo.getDescription());
        existVideo.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
        ofy().save().entity(existVideo).now();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(new JsonResponse()
                .setStatus(HttpServletResponse.SC_OK)
                .setMessage("Update Success!")
                .setData(existVideo)
                .toJsonString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String strId = req.getParameter("id");
        if (strId == null || strId.length() == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_BAD_REQUEST)
                    .setMessage("Bad request!")
                    .toJsonString());
            return;
        }
        Video existVideo = ofy().load().type(Video.class).id(Long.parseLong(strId)).now();
        if (existVideo == null || existVideo.isDeactive()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println(new JsonResponse()
                    .setStatus(HttpServletResponse.SC_NOT_FOUND)
                    .setMessage("Video is not found or has been deleted!")
                    .toJsonString());
            return;
        }
        existVideo.setStatus(-1);
        existVideo.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
        existVideo.setDeletedAt(Calendar.getInstance().getTimeInMillis());
        ofy().save().entity(existVideo).now();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(new JsonResponse()
                .setStatus(HttpServletResponse.SC_OK)
                .setMessage("Delete Success!")
                .toJsonString());
    }
}
