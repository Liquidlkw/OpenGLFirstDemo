//对于我们定义的每个单一的顶点，顶点着色器都会调用一次
//当它被调用时，a_Position属性里会接受当前顶点
attribute vec4 a_Position;//(x,y,z,w) 默认：(0,0,0,1)

//顶点着色器入口
void main() {
    //顶点着色器：确定每个顶点的最终位置。
    //图元装配阶段：根据指定的方式，把顶点组装成点、线、三角形等。
    gl_Position = a_Position;
}
