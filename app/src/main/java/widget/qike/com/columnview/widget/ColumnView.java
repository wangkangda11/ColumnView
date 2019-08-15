package widget.qike.com.columnview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

import widget.qike.com.columnview.util.DensityUtils;

/**
 * 柱状图
 * 作者：漆可 on 2016/12/14 10:15
 */
public class ColumnView extends View
{
    private int mWidth;
    private int mHeight;

    private final static int X_LAB = 0; //表示x轴绘制
    private final static int Y_LAB = 1; //y轴的绘制

    private Paint mTabLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int color_label_line = Color.parseColor("#DDDDDD"); //线条颜色
    private int mLineStroke = 1;//线条宽度
    private int mLabelCount = 10; //标尺线条数目

    /**
     * 图标所能显示的数值范围，
     * 最大值比能显示的极值大20%，防止如果出现较大数值时，矩形过高而过于贴近顶部
     */
    private float maxValue = 100 * 1.2f;
    private float minValue = 20;

    private float[] mColumnData;    //模拟数据

    private String mAxisXTitle = "销售项"; //x轴含义
    private String mAxisYTitle = "销售额"; //y轴含义

    private String[] mColumnName; //每个矩形数据项名称，即x轴刻度值
    private String[] mAxisYName;  //y轴刻度值

    private int mAxisTitleSize = DensityUtils.dp2px(getContext(), 12); //坐标轴标题大小
    private int mAxisNameTextSize = DensityUtils.dp2px(getContext(), 10); //坐标轴刻度值文字大小
    private int mAxisTextDistance = DensityUtils.dp2px(getContext(), 6); // 坐标轴标题文字与刻度值之间的距离
    private int mColumnMarginRight = DensityUtils.dp2px(getContext(), 3); // 矩形离Y轴的距离

    private int mMaxSubcolumnWidth = DensityUtils.dp2px(getContext(), 40);

    //坐标轴到view边界的留出的空间，用于绘制文字
    private int mPaddingLabLine = mAxisTitleSize + mAxisNameTextSize + 2 * mAxisTextDistance;


    public ColumnView(Context context)
    {
        this(context, null);
    }

    public ColumnView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ColumnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        mTabLinePaint.setColor(color_label_line);
        mTabLinePaint.setStrokeWidth(mLineStroke);

        initData();
    }

    private void initData()
    {
        //生成数据
        mColumnData = new float[8];
        for (int i = 0; i < mColumnData.length; i++)
        {
            mColumnData[i] = new Random().nextInt(80) + 20;
        }

        //Y轴刻度
        mColumnName = new String[8];
        for (int i = 0; i < mColumnData.length; i++)
        {
            mColumnName[i] = "数据" + String.valueOf(i + 1);
        }

        //y轴刻度
        mAxisYName = new String[mLabelCount];
        int v = (int) ((maxValue - minValue) / mLabelCount * 100);
        float value = v / 100f; //转成精确小数点后俩位的小数
        for (int i = 0; i < mAxisYName.length; i++)
        {
            String axisName = value * i + minValue + "";
            mAxisYName[i] = axisName;
        }
    }

    @Override
    public void layout(int l, int t, int r, int b)
    {
        super.layout(l, t, r, b);

        int width = getWidth();
        int height = getHeight();

        //兼容性处理，获取view的可绘制区域的宽高
        mWidth = width - getPaddingLeft() - getPaddingRight();
        mHeight = height - getPaddingBottom() - getPaddingTop();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        drawLabLine(canvas);
        drawAxis(canvas);
        drawSubcolumn(canvas);
    }

    //画柱状矩形
    private void drawSubcolumn(Canvas canvas)
    {
        mTabLinePaint.setColor(Color.parseColor("#4476AB"));

        //每一条数据占有的宽度
        int cellWidth = (int) (mWidth - mPaddingLabLine - mColumnMarginRight) / mColumnData.length;
        cellWidth = cellWidth > mMaxSubcolumnWidth ? mMaxSubcolumnWidth : cellWidth; //限制柱状图的最大宽度

        //矩形区有效绘制高度
        float height = getHeight() - getPaddingBottom() - mPaddingLabLine;
        float proportion = height / (maxValue - minValue); //高度数值比例尺

        mTabLinePaint.setStyle(Paint.Style.FILL);
        for (int j = 0; j < mColumnData.length; j++)
        {
            int startPos = mPaddingLabLine + mColumnMarginRight + cellWidth * j;
            float top =height - proportion * (mColumnData[j] - minValue);
            RectF rectF = new RectF(startPos + cellWidth / 5, top, startPos + cellWidth * 4 / 5, height);

            canvas.drawRect(rectF, mTabLinePaint);
        }
    }

    //画刻度值
    private void drawAxis(Canvas canvas)
    {
        drawAxisTitle(canvas, mAxisYTitle, Y_LAB);
        drawAxisTitle(canvas, mAxisXTitle, X_LAB);

        drawAxisName(canvas, Y_LAB, mAxisYName);
        drawAxisName(canvas, X_LAB, mColumnName);
    }

    // 画刻度值
    private void drawAxisName(Canvas canvas, int labelType, String[]  axisName)
    {
        mTabLinePaint.setTextSize(mAxisNameTextSize);

        //计算绘制坐标
        PointF[] pointFs = calculateAxisNamePosition(labelType, axisName);

        for (int i = 0; i < axisName.length; i++)
        {
            canvas.drawText(axisName[i], pointFs[i].x, pointFs[i].y, mTabLinePaint);
        }
    }

    @NonNull
    private PointF[] calculateAxisNamePosition(int labelType, String[] axisName)
    {
        PointF[] pointFs = new PointF[axisName.length];
        if (labelType == X_LAB)  //x轴
        {
            int textHeight = getTextHeight();

            int cellWidth = (mWidth - mPaddingLabLine - mColumnMarginRight) / this.mColumnName.length;
            final int y = mHeight - mPaddingLabLine + textHeight;
            for (int i = 0; i < pointFs.length; i++)
            {
                float textWidth = mTabLinePaint.measureText(axisName[i]);
                PointF pointF = new PointF();
                pointF.x = mPaddingLabLine + mColumnMarginRight + i * cellWidth + textWidth / 2;
                pointF.y = y;
                pointFs[i] = pointF;
            }
        } else
        {
            float height = getHeight() - getPaddingBottom() - mPaddingLabLine;
            float ceilHeight = (mHeight - mAxisTitleSize) / mLabelCount;

            for (int i = 0; i < pointFs.length; i++)
            {
                PointF pointF = new PointF();
                float textWidth = mTabLinePaint.measureText(axisName[i]);
                pointF.x = mPaddingLabLine - textWidth - mAxisTextDistance / 3;
                pointF.y = height - i * ceilHeight;
                pointFs[i] = pointF;
            }
        }
        return pointFs;
    }

    //绘制坐标轴名称
    private void drawAxisTitle(Canvas canvas, String axisTitle, int labelType)
    {
        mTabLinePaint.setTextSize(mAxisTitleSize);
        int textHeight = getTextHeight();
        if (labelType == X_LAB)
        {
            //x轴
            canvas.drawText(mAxisXTitle, mWidth / 2, mHeight - textHeight + mAxisTextDistance, mTabLinePaint);
        } else
        {
            //y轴
            canvas.save();
            //注意中心点的位置，需要稍微右移一点，防止文字太过于左偏
            canvas.rotate(-90, getPaddingLeft(), mHeight / 3 - textHeight);
            canvas.drawText(axisTitle, getPaddingLeft(), mHeight / 3, mTabLinePaint);
            canvas.restore();
        }
    }

    //获取字符串的高度
    private int getTextHeight()
    {
        Paint.FontMetricsInt fontMetrics = mTabLinePaint.getFontMetricsInt();
        return fontMetrics.bottom - fontMetrics.top;
    }

    //画坐标轴
    private void drawLabLine(Canvas canvas)
    {
        mTabLinePaint.setColor(color_label_line);
        //x轴起始点（也是Y轴的起点），终点
        float startX = getPaddingLeft() + mPaddingLabLine;
        float startY = getHeight() - getPaddingBottom() - mPaddingLabLine;
        float stopX = getWidth() - getPaddingRight();
        float stopY = startY;

        canvas.drawLine(startX, startY, stopX, stopY, mTabLinePaint); //x轴
        canvas.drawLine(startX, startY, startX, getPaddingTop(), mTabLinePaint); //y轴

        //画标尺
        float height = startY;
        float ceilHeight = (mHeight - mAxisTitleSize) / mLabelCount;
        for (int i = 0; i < mLabelCount; i++)
        {
            canvas.save();
            canvas.translate(0, -ceilHeight * i);
            canvas.drawLine(startX, startY, stopX, stopY, mTabLinePaint);
            canvas.restore();
        }
    }
}
