package cn.explink.b2c.tpsdo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.explink.enumutil.FlowOrderTypeEnum;
import cn.explink.enumutil.TPSFlowOrderTypeEnum;

@Service
public class DmpTpsTrackMappingService {

	private Map<Integer,Integer> map=null;
	public int getTpsOperateType(int dmpOperateType){
		if(map==null){
			init();
		}
		
		Integer value=map.get(dmpOperateType);
		if(value==null){
			//throw new RuntimeException("没有对应的TPS操作环节映射,dmpOperateType="+dmpOperateType);
			return -1;
		}
		
		return value.intValue();
	}
	
	public void init(){
		map=new HashMap<Integer,Integer>();
		map.put(FlowOrderTypeEnum.RuKu.getValue(), TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.ZhongZhuanZhanRuKu.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.TuiHuoZhanRuKu.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.FenZhanDaoHuoSaoMiao.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		
		map.put(FlowOrderTypeEnum.ChuKuSaoMiao.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		map.put(FlowOrderTypeEnum.ZhongZhuanZhanChuKuSaoMiao.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		map.put(FlowOrderTypeEnum.TuiHuoChuKuSaoMiao.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		map.put(FlowOrderTypeEnum.TuiHuoChuZhan.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		map.put(FlowOrderTypeEnum.TuiGongYingShangChuKu.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		
		map.put(FlowOrderTypeEnum.FenZhanLingHuo.getValue(),TPSFlowOrderTypeEnum.deliveryScan.getValue());
		
		//map.put(FlowOrderTypeEnum.YiFanKui.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());//?????
		map.put(FlowOrderTypeEnum.ShangMenTuiChengGong.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());
		map.put(FlowOrderTypeEnum.ShangMenHuanChengGong.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());
		
		map.put(FlowOrderTypeEnum.FenZhanZhiLiu.getValue(),TPSFlowOrderTypeEnum.retensionScan.getValue());
		
		map.put(FlowOrderTypeEnum.ZhongZhuanChuKuSaoMiao.getValue(),TPSFlowOrderTypeEnum.transferScan.getValue());
		map.put(FlowOrderTypeEnum.JuShou.getValue(),TPSFlowOrderTypeEnum.rejectScan.getValue());

	//------------- below mapping is no provided by cara
		//map.put(FlowOrderTypeEnum.DaoRuShuJu.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.TiHuo.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.TiHuoYouHuoWuDan.getValue(),TPSFlowOrderTypeEnum..getValue());
		map.put(FlowOrderTypeEnum.YouHuoWuDan.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.FenZhanDaoHuoYouHuoWuDanSaoMiao.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.ZhongZhuanZhanYouHuoWuDanRuKu.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.TuiHuoZhanYouHuoWuDanRuKu.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		map.put(FlowOrderTypeEnum.TuiHuoZhanZaiTouSaoMiao.getValue(),TPSFlowOrderTypeEnum.deliveryScan.getValue());
		map.put(FlowOrderTypeEnum.PeiSongChengGong.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());//???????????????
		map.put(FlowOrderTypeEnum.ShangMenTuiChengGong.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());
		map.put(FlowOrderTypeEnum.ShangMenHuanChengGong.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());
		map.put(FlowOrderTypeEnum.BuFenTuiHuo.getValue(),TPSFlowOrderTypeEnum.rejectScan.getValue());
		map.put(FlowOrderTypeEnum.ShangMenJuTui.getValue(),TPSFlowOrderTypeEnum.receiveFAILURE.getValue());
		map.put(FlowOrderTypeEnum.HuoWuDiuShi.getValue(),TPSFlowOrderTypeEnum.receiveFAILURE.getValue());
		map.put(FlowOrderTypeEnum.GongYingShangJuShouFanKu.getValue(),TPSFlowOrderTypeEnum.inboundScan.getValue());
		//map.put(FlowOrderTypeEnum.CheXiaoFanKui.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.ChongZhengJiaoYi.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.ErJiFenBo.getValue(),TPSFlowOrderTypeEnum..getValue());
		map.put(FlowOrderTypeEnum.KuDuiKuTuiHuoChuKu.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		map.put(FlowOrderTypeEnum.EeJiZhanTuiHuoChuZhan.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		//map.put(FlowOrderTypeEnum.GongHuoShangTuiHuoChenggong.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.UpdateDeliveryBranch.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.DaoCuoHuoChuLi.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.BeiZhu.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.ShouGongXiuGai.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.PosZhiFu.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.YiChangDingDanChuLi.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.DingDanLanJie.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.ShenHeWeiZaiTou.getValue(),TPSFlowOrderTypeEnum..getValue());
		map.put(FlowOrderTypeEnum.YiShenHe.getValue(),TPSFlowOrderTypeEnum.signInScan.getValue());//
		//map.put(FlowOrderTypeEnum.BaoGuoweiDao.getValue(),TPSFlowOrderTypeEnum..getValue());
		//map.put(FlowOrderTypeEnum.ZhongZhuanyanwu.getValue(),TPSFlowOrderTypeEnum..getValue());
		map.put(FlowOrderTypeEnum.ShouGongdiushi.getValue(),TPSFlowOrderTypeEnum.receiveFAILURE.getValue());
		//map.put(FlowOrderTypeEnum.ZiTiYanWu.getValue(),TPSFlowOrderTypeEnum..getValue());
		map.put(FlowOrderTypeEnum.KuDuiKuChuKuSaoMiao.getValue(),TPSFlowOrderTypeEnum.outboundScan.getValue());
		//map.put(FlowOrderTypeEnum.UpdatePickBranch.getValue(),TPSFlowOrderTypeEnum..getValue());

	}
}
