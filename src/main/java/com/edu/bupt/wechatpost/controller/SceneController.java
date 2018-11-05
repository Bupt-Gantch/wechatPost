package com.edu.bupt.wechatpost.controller;

import com.alibaba.fastjson.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/scene")
public class SceneController {
    private final static String BASEURL = "http://47.104.8.164:8800/api/v1/scene/";

    private static OkHttpClient client = new OkHttpClient();


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addScene(@RequestBody JSONObject message) throws IOException {
        System.out.println("\n添加场景============");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, message.toJSONString());
        Request request = new Request.Builder()
                .url(BASEURL + "add")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println(result);
        }
        return result;
    }

    @RequestMapping(value = "/getAllScene/{customerId}", method = RequestMethod.GET)
    @ResponseBody
    public String getAllScene(@PathVariable("customerId") Integer customerId)throws IOException{
        System.out.println("\n获取所有场景============");
        Request request = new Request.Builder()
                .url(BASEURL + "getAllScene/" + customerId)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println(result);
        }
        return result;
    }

    @RequestMapping(value = "/getSceneByGateway/{gateway_name}", method = RequestMethod.GET)
    @ResponseBody
    public String getSceneByGateway(@PathVariable("gateway_name") String gateway_name)throws IOException{
        System.out.println("\n通过网关名获取场景============");
        Request request = new Request.Builder()
                .url(BASEURL + "getSceneByGateway/" + gateway_name)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println(result);
        }
        return result;
    }

    @RequestMapping(value = "/deleteScene/{scene_id}", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteScene(@PathVariable("scene_id")Integer scene_id)throws IOException{
        System.out.println("\n删除场景============");
        Request request = new Request.Builder()
                .url(BASEURL + "deleteScene/" + scene_id)
                .delete()
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println("\n删除成功" + result);
        }
        return result;
    }

    @RequestMapping(value = "/bindSelector", method = RequestMethod.POST)
    @ResponseBody
    public String bindSceneSelector(@RequestBody JSONObject selectorInfo)throws IOException {
        System.out.println("\n绑定场景============");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, selectorInfo.toJSONString());
        Request request = new Request.Builder()
                .url(BASEURL + "bindSelector")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println(result);
        }
        return result;
    }

    @RequestMapping(value = "/getScene/{scene_id}", method = RequestMethod.GET)
    @ResponseBody
    public String getScene(@PathVariable("scene_id") Integer scene_id)throws IOException{
        System.out.println("\n获取场景============");
        Request request = new Request.Builder()
                .url(BASEURL + "getScene/" + scene_id)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println(result);
        }
        return result;
    }

    @RequestMapping(value = "/useScene/{scene_id}", method = RequestMethod.POST)
    @ResponseBody
    public String useScene(@PathVariable Integer scene_id)throws IOException {
        System.out.println("\n调用场景============");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, "{}");
        Request request = new Request.Builder()
                .url(BASEURL + "useScene/" + scene_id)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String result = new String();
        if(response.isSuccessful()){
            result = response.body().string();
            System.out.println(result);
        }
        return result;
    }
}
