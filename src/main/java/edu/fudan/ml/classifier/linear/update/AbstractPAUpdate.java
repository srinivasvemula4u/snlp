package edu.fudan.ml.classifier.linear.update;

import edu.fudan.ml.loss.Loss;
import edu.fudan.ml.types.Instance;
import edu.fudan.ml.types.sv.HashSparseVector;

/**
 * 抽象参数更新类，采用PA算法
 * \mathbf{w_{t+1}} = \w_t + {\alpha^*(\Phi(x,y)- \Phi(x,\hat{y}))}.
 * \alpha =\frac{1- \mathbf{w_t}^T \left(\Phi(x,y) - \Phi(x,\hat{y})\right)}{||\Phi(x,y) - \Phi(x,\hat{y})||^2}.
 * @author Feng Ji
 *
 */
public abstract class AbstractPAUpdate implements Update {

	/**
	 * \mathbf{w_t}^T \left(\Phi(x,y) - \Phi(x,\hat{y})\right)
	 */
	protected float diffw;
	/**
	 * \Phi(x,y)- \Phi(x,\hat{y})
	 */
	protected HashSparseVector diffv;
	protected Loss loss;
	/**
	 * 是否使用样本的权重进行加权
	 */
	public boolean useInstWeight;

	public AbstractPAUpdate(Loss loss) {
		diffw = 0;
		diffv = new HashSparseVector();
		this.loss = loss;
	}

	/**
	 * 参数更新方法
	 * @param inst 样本实例
	 * @param weights 权重
	 * @param predict 预测答案
	 * @param c 步长阈值
	 * @return 预测答案和标准答案之间的损失
	 */
	public float update(Instance inst, float[] weights, Object predict, float c) {
		return update(inst, weights, inst.getTarget(), predict, c);
	}

	/**
	 * 参数更新方法
	 * @param inst 样本实例
	 * @param weights 权重
	 * @param target 对照答案
	 * @param predict 预测答案
	 * @param c 步长阈值
	 * @return 预测答案和对照答案之间的损失
	 */
	public float update(Instance inst, float[] weights, Object target,
			Object predict, float c) {

		int lost = diff(inst, weights, target, predict);
		if(lost==0)
			return 0f;
		float lamda = diffv.l2Norm2();

		if (diffw <= lost) {
			float alpha = (lost - diffw) / lamda;
			if(useInstWeight)
				alpha = alpha*inst.getWeight();
			if(alpha>c){
				alpha = c;
			}else{
				alpha=alpha;
			}
			int[] idx = diffv.indices();
			 
			for (int i = 0; i < idx.length; i++) {
				
				weights[idx[i]] += diffv.get(idx[i]) * alpha;
			}
		}
		
		diffv.clear();
		diffw = 0;
		
		return loss.calc(target, predict);
	}

	/**
	 * 计算预测答案和对照答案之间的距离
	 * @param inst 样本实例
	 * @param weights 权重
	 * @param target 对照答案
	 * @param predict 预测答案
	 * @return 预测答案和对照答案之间的距离
	 */
	protected abstract int diff(Instance inst, float[] weights, Object target,
			Object predict);

	protected void adjust(float[] weights, int ts, int ps) {
		assert (ts != -1 && ps != -1);
		diffv.put(ts, 1.0f);
		diffv.put(ps, -1.0f);
		diffw += weights[ts] - weights[ps];
	}
}
