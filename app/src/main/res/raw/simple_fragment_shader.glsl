// 浮点数据类型的默认精度
precision mediump float;
// ⼀个uniform会让每个顶点都使⽤同⼀个值
uniform vec4 u_Color;

void main() {
    gl_fragcolor = u_Color;
}
