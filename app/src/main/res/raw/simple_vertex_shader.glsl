//全局变量：所有顶点都使用相同的 uniform 值 4x4矩阵
uniform mat4 u_Matrix;

//对于我们定义的每个单一的顶点，顶点着色器都会调用一次
//当它被调用时，a_Position属性里会接受当前顶点
attribute vec4 a_Position;//(x,y,z,w) 默认：(0,0,0,1)
attribute vec4 a_Color;//(r,g,b,a) 默认：(0,0,0,1)

//varying变量：变量充当顶点着色器和片元着色器之间的数据传递桥梁
//OpenGL 会自动对 varying 变量进行插值计算，为每个片元生成相应的值
varying vec4 v_Color;

//顶点着色器入口
void main() {
    v_Color = a_Color;

    //顶点着色器：确定每个顶点的最终位置。
    //图元装配阶段：根据指定的方式，把顶点组装成点、线、三角形等。
    gl_Position = u_Matrix * a_Position;
    gl_PointSize = 10.0; // 点大小


}
