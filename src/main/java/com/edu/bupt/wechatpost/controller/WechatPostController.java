package com.edu.bupt.wechatpost.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edu.bupt.wechatpost.model.Comment;
import com.edu.bupt.wechatpost.model.LikeRelation;
import com.edu.bupt.wechatpost.model.Post;
import com.edu.bupt.wechatpost.model.ResponseMsg;
import com.edu.bupt.wechatpost.service.DataService;
import com.edu.bupt.wechatpost.service.PostCommentService;
import com.edu.bupt.wechatpost.service.WxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wechatPost")
public class WechatPostController {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private DataService dataService;

    @Autowired
    private WxService wxService;

    private final static Logger logger = LoggerFactory.getLogger(WechatPostController.class);


    @RequestMapping(value = "/posts", method = RequestMethod.GET)
    public ResponseMsg<List<Post>> findAllPost(@RequestParam(value = "openId", required = false)String openId,
                                               @RequestParam("page")int page){

        logger.info("查询所有消息...");

        try {
            List<Post> posts = postCommentService.findAllPosts(openId, page);
            logger.info("查询成功, 一共有{}条消息", posts.size());
            return new ResponseMsg<>(0, "success", posts);

        } catch (Exception e){
            logger.info("查询失败");
            e.printStackTrace();
            return new ResponseMsg<>(1, "exception", null);
        }
    }


    @RequestMapping(value = "/posts/search", method = RequestMethod.GET)
    public ResponseMsg<List<Post>> findPosts(@RequestParam("searchText")String searchText,
                                             @RequestParam("page")int page){

        logger.info("搜索消息...");

        try {
            List<Post> posts = postCommentService.searchPosts(searchText, page);
            logger.info("搜索成功, 一共有{}条消息", posts.size());
            return new ResponseMsg<>(0, "success", posts);

        } catch (Exception e){
            logger.info("搜索失败");
            e.printStackTrace();
            return new ResponseMsg<>(1, "exception", null);
        }
    }


    @RequestMapping(value = "/post/add/text", method = RequestMethod.POST)
    public ResponseMsg<Object> addPost(@RequestBody JSONObject message){

        logger.info("发布消息...");

        // check for content if its illegal
        String content = message.getString("content");
        String accessToken = wxService.getAccessToken();
        if (!wxService.msgSecCheck(accessToken, content)) {
            logger.warn("发布失败,文字包含敏感信息");
            return new ResponseMsg<>(-1, "文字包含敏感信息", content);
        }

        // TODO 从图片链接获取图片校验是否含有敏感信息
        String images = message.getString("images");

        String openId = message.getString("openId");
        if (openId == null || openId.equals("")){
            logger.warn("发布失败,openid不能为空");
            return new ResponseMsg<>(2, "openid为空", null);
        }

        String location = message.getString("location");
        String nickName = message.getString("nickName");
        String avatar = message.getString("avatar");
        java.text.DateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeFormat = f.format(new Date());

        try {
            Post myPost = new Post(openId, avatar, nickName, timeFormat, content, images, location, 0);
            if ( 0 == postCommentService.publishPost(myPost)) {
                logger.error("发布图文消息失败，数据库插入异常");
            }
            logger.info("发布成功");
            return new ResponseMsg<>(0, "发布成功", myPost);
        } catch(Exception e){
            logger.info("发布失败");
            e.printStackTrace();
            return new ResponseMsg<>(3, "发布失败", null);
        }
    }


    @RequestMapping(value = "/post/add", method = RequestMethod.POST)
    public ResponseMsg<Object> publishTextAndPicture(@RequestBody JSONObject message){
        logger.info("发布消息...");

        String accessToken = wxService.getAccessToken();

        // check for content weather its illegal
        String content = message.getString("content");
        if (!wxService.msgSecCheck(accessToken, content)){
            logger.warn("发布失败，文字包含敏感信息");
            return new ResponseMsg<>(-1, "文字包含敏感信息", content);
        }

        // check for image weather its illegal
        MultipartFile image = (MultipartFile)message.get("image");
        if (!wxService.imgSecCheck(accessToken, image)){
            logger.warn("发布失败，图片包含敏感信息");
            return new ResponseMsg<>(-1, "图片包含敏感信息", image);
        }

        String openId = message.getString("openId");
        if (openId == null || openId.equals("")){
            logger.warn("发布失败,openid不能为空");
            return new ResponseMsg<>(2, "openid为空", null);
        }

        String location = message.getString("location");
        String nickName = message.getString("nickName");
        String avatar = message.getString("avatar");
        java.text.DateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeFormat = f.format(new Date());

        try{
            String staticImageUrl = dataService.uploadImage(image);
            if(!staticImageUrl.equals("")){
                Post myPost = new Post(openId, avatar, nickName, timeFormat, content, staticImageUrl, location, 0);
                postCommentService.publishPost(myPost);
                logger.info("发布成功");
                return new ResponseMsg<>(0, "发布成功", myPost);
            } else {
                logger.info("图片上传失败");
                return new ResponseMsg<>(4, "图片上传失败", image);
            }
        } catch(Exception e){
            logger.info("发布失败");
            e.printStackTrace();
            return new ResponseMsg<>(3, "发布失败", null);
        }
    }


    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    public ResponseMsg<Object> uploadImage(@RequestParam("image") MultipartFile image){
        if (image == null) {
            logger.warn("上传图片为空");
            return new ResponseMsg<>(5,"上传图片为空", null);
        }

        String accessToken = wxService.getAccessToken();
        if (!wxService.imgSecCheck(accessToken, image)){
            logger.warn("图片包含敏感信息");
            return new ResponseMsg<>(-1, "图片包含敏感信息", image);
        }

        String imageUrl = dataService.uploadImage(image);

        return new ResponseMsg<>(0, "图片上传成功", imageUrl);
    }


    @RequestMapping(value = "/download",method=RequestMethod.GET)
    public void downloadImage(String imageName, HttpServletRequest request, HttpServletResponse response){

        dataService.downloadImage(imageName, request, response);
    }


    @RequestMapping(value = "/post/del/{pId}", method = RequestMethod.DELETE)
    public ResponseMsg<Integer> deletePost(@PathVariable("pId") Integer pId){

        logger.info("删除消息...");

        int delete = postCommentService.deletePost(pId);
        if (delete == 0) {
            logger.warn("消息 {} 删除失败", pId);
            return new ResponseMsg(6, "消息删除失败", pId);
        }

        logger.info("消息 {} 删除成功", pId);
        return new ResponseMsg<>(0, "消息删除成功", pId);
    }


    @RequestMapping(value = "/favorite", method = RequestMethod.POST)
    public ResponseMsg<Integer> updateFavoriteNum(@RequestBody JSONObject message){
        Integer pId = message.getInteger("pId");
        Integer num = message.getInteger("num");
        String nickName = message.getString("nickName");
        String avator = message.getString("avator");
        String openid = message.getString("openId");  /// 主动者

        if(wxService.isLike(openid,pId)) { // 如果已经点赞了
            wxService.unfavorite(openid, pId);  //  取消点赞
            return new ResponseMsg(0, "取消点赞", null);

        }else{  // 如果没有点赞
            Post p = postCommentService.findPost(pId);
            if (p == null) {
                logger.warn("点赞失败，消息 {}", pId);
                return new ResponseMsg<>(1,  "该消息不存在", null);
            }

            String touser = p.getOpenId();  // 被动者openid
            try {  // 添加对应关系

                wxService.favorite(pId, 1);  // 点赞
                // 插入点赞记录
                Integer l_id = wxService.addLikeRelation(new LikeRelation(pId, nickName));
                // 插入提示记录
                postCommentService.addTip(touser, avator, 1, l_id);

                return new ResponseMsg<>(0, "点赞成功", null);

            }catch(Exception e){
                logger.warn("点赞失败，消息 {}", pId);
                wxService.unfavorite(touser, pId);
                e.printStackTrace();
                return new ResponseMsg<>(7, "点赞失败", null);
            }

        }
    }


    @RequestMapping(value = "/comment/add", method = RequestMethod.POST)
    public ResponseMsg<String> addComment(@RequestBody JSONObject message){

        logger.info("消息评论...");

        // check for content if its illeagle
        String accessToken = wxService.getAccessToken();
        String cContent = message.getString("cContent");
        if (!wxService.msgSecCheck(accessToken, cContent)) {
            logger.warn("发布失败,文字包含敏感信息");
            return new ResponseMsg<>(-1, "文字包含敏感信息", cContent);
        }

        Integer pId = message.getInteger("pId");
        String nickName = message.getString("nickName");
        String avator = message.getString("avator");

        String touser = "";
        Comment comment = new Comment(pId, nickName, cContent);

        try{
            Post post = postCommentService.findPost(pId);
            if(null == post){
                return new ResponseMsg<>(1, "该消息不存在", String.valueOf(pId)); // 评论对应的消息为空
            }

            touser = post.getOpenId();
            int id = postCommentService.addComment(comment);
            if (id >= 0) {
                postCommentService.addTip(touser, avator, 2, id);
                return new ResponseMsg<>(0, "评论发布成功", cContent); // 评论成功并且添加提示

            } else {
                postCommentService.deleteComment(id);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.warn("评论失败，插入记录失败");
        return new ResponseMsg<>(8, "插入失败", null);
    }

    @RequestMapping(value = "/comment/del/{cId}", method = RequestMethod.DELETE)
    public ResponseMsg<Integer> deleteComment(@PathVariable("cId")Integer cId) {

        logger.info("删除评论...");

        int delete = postCommentService.deleteComment(cId);
        if (delete != 0) {
            logger.info("删除评论成功, {}", cId);
            return new ResponseMsg<>(0, "删除评论成功", cId);
        }

        logger.info("删除评论失败, {}", cId);
        return new ResponseMsg<>(9, "删除评论失败", cId);
    }


    @RequestMapping(value = "/getOpenId", method = RequestMethod.POST)
    public String getOpenId(@RequestBody JSONObject message){
        return wxService.getOpenId(message);
    }


    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    public int judgeFollow(@RequestBody JSONObject message){
        return wxService.follow(message);
    }


    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void registe(@RequestParam(value = "unionid",required = false)String unionid,
                        @RequestParam(value = "openid", required = false)String oa_openid){
        if(!"".equals(unionid)){
            logger.info("receive message from wechat official account, unionid="+unionid+"\topenid="+oa_openid);
            wxService.registe(unionid, oa_openid);
        }
    }



    @RequestMapping(value = "/tips/{openid}", method = RequestMethod.GET)
    public JSONArray getAllTips(@PathVariable("openid")String openid){
        return wxService.getTips(openid);
    }


    @RequestMapping(value = "/tips/unread/{openid}", method = RequestMethod.GET)
    public JSONArray getUnreadTips(@PathVariable("openid")String openid){
        return wxService.getUnreadTips(openid);
    }


    @RequestMapping(value = "/tips/read/{openid}", method = RequestMethod.GET)
    public void readTips(@PathVariable("openid")String openid){
        wxService.readTips(openid);
    }

}