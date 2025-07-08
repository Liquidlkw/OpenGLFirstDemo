// 浮点数据类型的默认精度
precision mediump float;
// ⼀个uniform会让每个顶点都使⽤同⼀个值
//uniform的位置并不是事先指定的，因此，⼀旦程序链接成功，我们就要查询这个位置。
//⼀个uniform的位置在⼀个程序对象中是唯⼀的
uniform vec4 u_Color;

void main() {
    gl_fragcolor = u_Color;
}
