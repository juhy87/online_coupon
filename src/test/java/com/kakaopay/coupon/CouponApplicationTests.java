package com.kakaopay.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import com.kakaopay.coupon.biz.coupon.request.CreateCouponRequest;
import com.kakaopay.coupon.biz.user.request.UserRequest;

import org.json.JSONObject;
import org.junit.FixMethodOrder;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@FixMethodOrder(MethodSorters.DEFAULT)
@AutoConfigureMockMvc
class CouponApplicationTests {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    public static String accessToken = null;

    public static List<LinkedHashMap> myCouponList = null;
    public static List<LinkedHashMap> myTodayExiredCouponList = null;

    /**
     * 1. 회워가입 및 로그인
     * @throws Exception
     */
    @Test
    public void Test_A() throws Exception{

        System.out.println("[======================[signin_login]======================");

        UserRequest userRequest = UserRequest.builder()
                .userId("kakaoId")
                .password("password1").build();
        String userInfo = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/signin").contentType(MediaType.APPLICATION_JSON)
                .content(userInfo))
                .andExpect(status().isOk())
                .andDo(print());


        ResultActions result
                = mockMvc.perform(post("/auth").contentType(MediaType.APPLICATION_JSON)
                .content(userInfo))
                .andExpect(status().isOk())
                .andDo(print());

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        accessToken = jsonParser.parseMap(resultString).get("token").toString();

    }

    /**
     * 2. 쿠폰 생성 (총 3개, 만료일 : 2021-06-23 (1개), Today(2개))
     * @throws Exception
     */
    @Test
    public void Test_B() throws Exception{
        System.out.println("[======================[createCoupon1]======================");
        CreateCouponRequest createCouponRequest = CreateCouponRequest.builder()
                .count(1)
                .year(2021)
                .month(6)
                .day(23).build();

        String coupon = objectMapper.writeValueAsString(createCouponRequest);

        mockMvc.perform(post("/createCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(coupon))
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("[======================[createCoupon2]======================");

        LocalDate currentdate = LocalDate.now();
        int day = currentdate.getDayOfMonth();
        int month = currentdate.getMonthValue();
        int year = currentdate.getYear();

        createCouponRequest = CreateCouponRequest.builder()
                .count(2)
                .year(year)
                .month(month)
                .day(day).build();

        coupon = objectMapper.writeValueAsString(createCouponRequest);
        mockMvc.perform(post("/createCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .content(coupon))
                .andExpect(status().isOk())
                .andDo(print());

    }

    /**
     * 3. 코드 랜덤 1개씩 3번 발행
     * @throws Exception
     */
    @Test
    public void Test_C() throws Exception{
        System.out.println("[======================[issue Coupon]======================");

        for(int count = 0 ; count < 3 ; count++){

            mockMvc.perform(post("/issueCoupon")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

    }

    /**
     * 4. 나의 쿠폰List 조회
     * @throws Exception
     */
    @Test
    public void Test_D() throws Exception{
        System.out.println("[======================[getCoupons]======================");

        ResultActions result = mockMvc.perform(get("/getCoupons")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());


        JacksonJsonParser jsonParser = new JacksonJsonParser();
        String resultString = result.andReturn().getResponse().getContentAsString();
        myCouponList = jsonParser.parseList(resultString).stream().map(s->(LinkedHashMap)s).collect(Collectors.toList());


    }

    /**
     * 5. 쿠폰 발급 취소
     * @throws Exception
     */
    @Test
    public void Test_E() throws Exception{
        System.out.println("[======================[cancelCoupon]======================");
        if(myCouponList != null && myCouponList.size() > 0){
            String code = myCouponList.get(0).get("code").toString();
            Coupon couponRequest = Coupon.builder().code(code).build();

            String coupon = objectMapper.writeValueAsString(couponRequest);

            mockMvc.perform(post("/cancelCoupon")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .content(coupon))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

    }

    /**
     * 6. 오늘 만료되는 쿠폰List 조회
     * @throws Exception
     */
    @Test
    public void Test_F() throws Exception{
        System.out.println("[======================[getTodayExpiredCoupon]======================");
        ResultActions result = mockMvc.perform(get("/getTodayExpiredCoupon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());


        JacksonJsonParser jsonParser = new JacksonJsonParser();
        String resultString = result.andReturn().getResponse().getContentAsString();
        myTodayExiredCouponList = jsonParser.parseList(resultString).stream().map(s->(LinkedHashMap)s).collect(Collectors.toList());


    }

//
//    @Test
//    public void test() throws Exception{
//
//        System.out.println("======================[Test 시작]======================");
//
//        mockMvc.perform(get("/home"))
//                .andExpect(status().isOk());
////                .andExpect(content().string("Welcome Home"));
//    }

}
