package com.kakaopay.coupon.biz.coupon.service;

import com.kakaopay.coupon.biz.constant.CouponStatus;
import com.kakaopay.coupon.biz.coupon.task.CouponSaveTask;
import com.kakaopay.coupon.biz.coupon.entity.Coupon;
import com.kakaopay.coupon.biz.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Service("couponService")
public class CouponServiceImple implements CouponService {

    private static int maxThreadCnt = 5; //최대 쓰레드 수

    private static int inPutRowCnt = 1000; //쓰레드별 입력행수

    private static boolean threadState = true; //쓰레드 그룹개수제한

    //쓰레드 그룹을 컨트롤 할 서비스 생성
    private static ExecutorService ex = Executors.newCachedThreadPool(new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r){
            return new Thread(r);
        }
    });

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public List<Coupon> createCoupons(long count, int year, int month, int day) {

        ArrayList<Coupon> coupons = new ArrayList<>();
        for(int i = 0; i < count; i++){
            coupons.add(new Coupon(CouponStatus.NOTYETUSED, year, month, day));
        }
        List<Coupon> couponList= couponRepository.saveAll(coupons);

        return couponList;
    }

    @Override
    public void createCoupons(MultipartFile file) {
        BufferedReader br = null;
        String line;
        int rowcount = 0; //max : 21억
        try {
            br = new BufferedReader(new InputStreamReader((FileInputStream)(file.getInputStream()), "euc-kr"));
            ArrayList<String> list = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                rowcount++;
                list.add(line);
                if (rowcount % inPutRowCnt == 0) {
                    Runnable r = new CouponSaveTask(couponRepository, (ArrayList<String>)(list.clone()));
                    saveThreadManage(r);
                    list.clear();
                }

            }

            //미처리 된 데이터에 대해 처리
            if(list.isEmpty()){
                System.out.println("파일 처리 완료 ");
            }else{
                Runnable r = new CouponSaveTask(couponRepository, (ArrayList<String>) list.clone());
                r.run();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //쓰레드 관리를 위한 쓰레드 관리함수
    private static void saveThreadManage(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);

        //활성중인 쓰레드의 개수가 최대 쓰레드 개수보다 작을 경우 EXECUTOR사용
        if(t.activeCount() < maxThreadCnt  && threadState){
            System.out.println("Active Thread count is : "+t.activeCount());
            ex.execute(t);
        }else{
            //최대 쓰레드 개수에 도달 시 executor에게 작업완료 후 종료명령
            if(ex.isTerminated()){
                //활성 쓰레드가 모두 종료되었을 경우 새 Executor 할당 후 실행
                threadState = true;
                ex = Executors.newCachedThreadPool(new ThreadFactory(){
                    @Override
                    public Thread newThread(Runnable r){
                        return new Thread(r);
                    }
                });
                ex.execute(t);
            }else{
                if(threadState){  //쓰레드 종료 명령이 호출되지 않은 경우만 수행
                    threadState = false;
                    System.out.println("Thread group Shutdown");
                    ex.shutdown();
                }
                t.run(); //쓰레드 종료 명령이 완료될 때까진 메인 클래스가 직접수행
            }
        }
    }


}
