package androidrn.myflowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuml
 * @explain 流式布局
 * @time 2017/12/26 20:32
 */

public class FlowLayout extends ViewGroup {



    /**
     * 用来保存每行的view的列表
     */
    private List<List<View>> mViewLinesList = new ArrayList<>();
    /**
     * 用来保存行高的列表
     */
    private List<Integer> mLineHeights = new ArrayList<>();

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 用来支持margin
     *
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);

    }

    /**
     * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获得它的父容器为它设置的测量模式和大小
        int iWidthMode = MeasureSpec.getMode(widthMeasureSpec);//宽度的测量模式
        int iHeithgMode = MeasureSpec.getMode(heightMeasureSpec);//高度的测量模式
        //如果是EXACTLY 那么就是获取的值
        int iWidthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);//  比如at_most中最多不能超过的值
        int iHeightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);

//        如果是warp_content情况下，记录宽和高
        int measureWidth = 0;//最终宽度
        int measureHeight = 0;//最终的高度
        int iCurrentLineW = 0;//每行的宽度
        int iCurrentLineH = 0;//每行的高度


        //如果测量模式是EXACTLY 也就是宽高设置成match_parent
        if (iWidthMode == MeasureSpec.EXACTLY && iHeithgMode == MeasureSpec.EXACTLY) {
            //那么获取的宽度默认值就是建议的值
            measureWidth = iWidthSpaceSize;
            measureHeight = iHeightSpaceSize;
        } else {
            //开始测量
            int childWidth;
            int childHeight;
            //用来存储每行的子view
            List<View> viewList = new ArrayList<>();
            //获取孩子的个数
            int childCount = getChildCount();

            //遍历所有child view
            for (int i = 0; i < childCount; i++) {
                //获取孩子View
                View childView = getChildAt(i);

                //测量每一个child的宽和高 参数把 默认的宽高传入进去
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                // 得到child的layoutParams
                MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                // 当前子空间实际占据的宽度
                childWidth = childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                // 当前子空间实际占据的高度
                childHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
                /**
                 * 如果加入当前child，超出最大宽度，则得到目前最大宽度给width，累加 然后换行
                 */
                if (iCurrentLineW + childWidth > iWidthSpaceSize) {
                    //1 记录当前行的信息

                    //宽度比较 获取最宽的宽度 用测量的宽度和当前的宽度进行比较
                    measureWidth = Math.max(measureWidth, iCurrentLineW);
                    //高度累加
                    measureHeight +=  iCurrentLineH;


                    //2,将当前viewLine添加至总的mViewLinesList,并将高度添加至总的高度list
                    mViewLinesList.add(viewList);
                    mLineHeights.add(iCurrentLineH);

                    //1,换行,记录新的一行的信息
                    //重新赋值新的一行的宽高
                    iCurrentLineW = childWidth;
                    iCurrentLineH = childHeight;

                    // 新建一行,那么viewList 也需要重新new一个
                    viewList = new ArrayList<View>();
                    viewList.add(childView);

                } else {
                    //1、行内宽度的叠加、高度比较
                    //如果没有大于父容器的宽度 那么宽度不断累加
                    iCurrentLineW +=  childWidth;
                    //高度比较 获取最高的高度
                    iCurrentLineH = Math.max(iCurrentLineH,childHeight );
                    // 2、添加至当前行的viewList中
                    viewList.add(childView);
                }
                /*****3、如果正好是最后一行需要换行**********/
                //如果正好是最后一行需要换行
                if (i == childCount - 1) {
                    //1 记录当前行的信息

                    //宽度比较 获取最宽的宽度 用测量的宽度和当前的宽度进行比较
                    measureWidth = Math.max(measureWidth, iCurrentLineW);
                    //高度累加
                    measureHeight +=  iCurrentLineH;


                    //2,将当前viewLine添加至总的mViewLinesList,并将高度添加至总的高度list
                    mViewLinesList.add(viewList);
                    mLineHeights.add(iCurrentLineH);


                }
            }


        }

        //最后需要调用这个 存储测量宽度和测量高度
        setMeasuredDimension(measureWidth, measureHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /**遍历摆放view**/
        int left, right, top, bottom;
        int curTop = 0;
        int curLeft = 0;
        //共有多少行
        int lineCount = mViewLinesList.size();
        //垂直方向遍历行
        for (int i = 0; i < lineCount; i++) {
            //这是第i行
            List<View> viewLine = mViewLinesList.get(i);
            //第i行有多少个childview
            int lineViewSize = viewLine.size();
            //遍历第i行
            for (int j = 0; j < lineViewSize; j++) {
                //获取当前行的chlidview
                View childView = viewLine.get(j);
                //获取当前childview的LayoutParams
                MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                //下面根据layoutparams 获取左上右下的距离 最后再调用chlidview.layout
                left = curLeft + layoutParams.leftMargin;
                top = curTop + layoutParams.topMargin;
                //右边的距离是当前距离左边的距离加上控件的宽度
                right = left + childView.getMeasuredWidth();
                //高度同理
                bottom = top + childView.getMeasuredHeight();
                //开始布局
                childView.layout(left, top, right, bottom);
                //currentLeft 叠加 布局一次currentLeft 需要向右移动
                curLeft += childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;

            }
            //换行后currentLeft置为0
            curLeft = 0;
            //换行后高度需要累加
            curTop += mLineHeights.get(i);
        }
        //布局完成后清空
        mViewLinesList.clear();
        mLineHeights.clear();


    }
}
