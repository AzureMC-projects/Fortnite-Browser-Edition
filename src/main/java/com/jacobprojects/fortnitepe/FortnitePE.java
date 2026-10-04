package com.jacobprojects.fortnitepe;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public final class FortnitePE {
    static final int W=1280,H=720; static long window;
    static double px=0,pz=0,py=0,yaw=0,pitch=0,health=100,shield=50,wood=500,velY=0;
    static boolean firing,aiming,buildMode,menu=true; static int buildType=0,weapon=1,ammo=30,kills=0;
    static final List<Bot> bots=new ArrayList<>(); static final List<Build> builds=new ArrayList<>(); static final List<Item> items=new ArrayList<>();
    static final Random rng=new Random(42);
    record Bot(double x,double z,double hp){} record Build(double x,double y,double z,int type){} record Item(double x,double z,int type){}

    public static void main(String[] a){
        GLFWErrorCallback.createPrint(System.err).set(); if(!glfwInit())throw new IllegalStateException("GLFW failed");
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,2);glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,1);
        window=glfwCreateWindow(W,H,"Fortnite PE - Performance Edition",0L,0L);if(window==0)throw new IllegalStateException("Window failed");
        glfwMakeContextCurrent(window);glfwSwapInterval(1);glfwShowWindow(window);GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);glEnable(GL_CULL_FACE);glClearColor(.43f,.70f,.92f,1);
        for(int i=0;i<16;i++)bots.add(new Bot(rng.nextDouble()*150-75,rng.nextDouble()*150-75,100));
        for(int i=0;i<30;i++)items.add(new Item(rng.nextDouble()*170-85,rng.nextDouble()*170-85,rng.nextInt(4)));
        glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);
        final double[] mx={0},my={0},lx={W/2.0},ly={H/2.0};glfwSetCursorPos(window,lx[0],ly[0]);
        glfwSetCursorPosCallback(window,(w,x,y)->{mx[0]=x-lx[0];my[0]=y-ly[0];lx[0]=x;ly[0]=y;});
        glfwSetMouseButtonCallback(window,(w,b,state,m)->{if(b==GLFW_MOUSE_BUTTON_LEFT&&state==GLFW_PRESS){if(menu){double[] c={0,0};glfwGetCursorPos(window,c);if(c[0]>430&&c[0]<850&&c[1]>330&&c[1]<430){menu=false;glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_DISABLED);glfwSetCursorPos(window,W/2,H/2);}}else firing=true;}if(b==GLFW_MOUSE_BUTTON_LEFT&&state==GLFW_RELEASE)firing=false;if(b==GLFW_MOUSE_BUTTON_RIGHT)aiming=state==GLFW_PRESS;});
        glfwSetKeyCallback(window,(w,k,s,v,m)->{if(v!=GLFW_PRESS)return;
            if(k==GLFW_KEY_ESCAPE){if(menu)glfwSetWindowShouldClose(w,true);else{menu=true;glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);}} if(k==GLFW_KEY_ENTER&&menu){menu=false;glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_DISABLED);}
            if(k==GLFW_KEY_F1){buildMode=true;buildType=0;} if(k==GLFW_KEY_F2){buildMode=true;buildType=1;} if(k==GLFW_KEY_F3){buildMode=true;buildType=2;}
            if(k==GLFW_KEY_R)ammo=30;if(k==GLFW_KEY_E)pickup();if(k>=GLFW_KEY_1&&k<=GLFW_KEY_5){weapon=k-GLFW_KEY_0;ammo=30;}
        });
        long last=System.nanoTime();
        while(!glfwWindowShouldClose(window)){long n=System.nanoTime();double dt=Math.min(.033,(n-last)/1e9);last=n;
            yaw-=mx[0]*.0024*(aiming?.45:1);pitch=Math.max(-1.15,Math.min(1.15,pitch-my[0]*.0024*(aiming?.45:1)));mx[0]=my[0]=0;
            update(dt);render();glfwSwapBuffers(window);glfwPollEvents();
        }glfwDestroyWindow(window);glfwTerminate();
    }

    static void update(double dt){if(menu)return;
        double speed=glfwGetKey(window,GLFW_KEY_LEFT_SHIFT)==GLFW_PRESS?12:7;if(aiming)speed*=.7;
        double fx=-Math.sin(yaw),fz=-Math.cos(yaw),rx=Math.cos(yaw),rz=-Math.sin(yaw),x=0,z=0;
        if(key(GLFW_KEY_W)){x+=fx;z+=fz;}if(key(GLFW_KEY_S)){x-=fx;z-=fz;}if(key(GLFW_KEY_D)){x+=rx;z+=rz;}if(key(GLFW_KEY_A)){x-=rx;z-=rz;}
        double l=Math.hypot(x,z);if(l>0){px+=x/l*speed*dt;pz+=z/l*speed*dt;}
        if(key(GLFW_KEY_SPACE)&&py<=0)velY=8;velY-=22*dt;py=Math.max(0,py+velY*dt);if(py==0)velY=0;
        if(firing&&!buildMode&&ammo>0){ammo--;shoot();}
        if(buildMode&&glfwGetMouseButton(window,GLFW_MOUSE_BUTTON_LEFT)==GLFW_PRESS&&wood>=10){
            builds.add(new Build(px+fx*5,buildType==1?1.5:0,pz+fz*5,buildType));wood-=10;buildMode=false;
        }
        for(int i=0;i<bots.size();i++){Bot b=bots.get(i);double dx=px-b.x,dz=pz-b.z,d=Math.hypot(dx,dz);
            if(d<25)hurt(8*dt);else if(d>8)bots.set(i,new Bot(b.x+dx/d*2*dt,b.z+dz/d*2*dt,b.hp));
        }
    }
    static boolean key(int k){return glfwGetKey(window,k)==GLFW_PRESS;}
    static void hurt(double d){double s=Math.min(shield,d);shield-=s;health-=d-s;if(health<=0){health=100;shield=50;px=pz=0;}}
    static void shoot(){double fx=-Math.sin(yaw),fz=-Math.cos(yaw);int hit=-1;double best=999;
        for(int i=0;i<bots.size();i++){Bot b=bots.get(i);double dx=b.x-px,dz=b.z-pz,d=Math.hypot(dx,dz),dot=(dx*fx+dz*fz)/Math.max(.01,d);if(d<80&&dot>.96&&d<best){best=d;hit=i;}}
        if(hit>=0){Bot b=bots.get(hit);double dmg=weapon==2?65:weapon==3?30:weapon==4?18:weapon==5?120:24;if(b.hp<=dmg){bots.remove(hit);kills++;}else bots.set(hit,new Bot(b.x,b.z,b.hp-dmg));}
    }
    static void pickup(){for(int i=0;i<items.size();i++){Item q=items.get(i);if(Math.hypot(q.x-px,q.z-pz)<4){if(q.type==0)ammo=30;if(q.type==1)wood+=100;if(q.type==2)shield=Math.min(100,shield+50);if(q.type==3)health=Math.min(100,health+30);items.remove(i);return;}}}

    static void render(){
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glMatrixMode(GL_PROJECTION);glLoadIdentity();perspective(aiming?58:72,(float)W/H,.1,500);
        glMatrixMode(GL_MODELVIEW);glLoadIdentity();double fx=-Math.sin(yaw),fz=-Math.cos(yaw);double camX=px-fx*(aiming?3.2:6.5),camY=3.0+py+pitch*.8,camZ=pz-fz*(aiming?3.2:6.5);lookAt(camX,camY,camZ,px,1.25+py,pz);
        world();for(Bot b:bots)character(b.x,0,b.z,false);for(Build b:builds)drawBuild(b);for(Item q:items)item(q);character(px,py,pz,true);hud();
    }
    static void world(){
        glColor3f(.20f,.55f,.20f);glBegin(GL_QUADS);v(-180,0,-180);v(180,0,-180);v(180,0,180);v(-180,0,180);glEnd();
        for(int x=-150;x<=150;x+=20)for(int z=-150;z<=150;z+=20)if((x*3+z*7)%31<18){push();translate(x,0,z);tree();pop();}
        for(int i=0;i<18;i++){push();translate((float)Math.sin(i*4.7)*95,.1f,(float)Math.cos(i*3.2)*95);rock();pop();}
        for(int i=0;i<8;i++){push();translate((float)Math.sin(i*9.1)*70,0,(float)Math.cos(i*7.3)*70);house();pop();}
    }
    static void tree(){glColor3f(.38f,.20f,.08f);cylinder(.35f,3,8);translate(0,2.8f,0);glColor3f(.06f,.45f,.12f);sphere(1.7f,10,7);translate(.5f,.6f,.2f);sphere(1.1f,10,7);}
    static void rock(){glColor3f(.35f,.38f,.42f);sphere(1.5f,8,5);}
    static void house(){glColor3f(.65f,.43f,.22f);cube(7,4,7);translate(0,3,0);glColor3f(.55f,.12f,.08f);pyramid(4.5f,3);}
    static void character(double x,double y,double z,boolean p){push();translate((float)x,(float)y,(float)z);
        glColor3f(p?.08f:.78f,p?.38f:.16f,p?.85f:.18f);cube(.9f,1.7f,.55f);translate(0,1.15f,0);glColor3f(1,.70f,.5f);sphere(.46f,12,8);translate(0,.55f,0);glColor3f(p?.05f:.08f,p?.28f:.12f,p?.7f:.65f);sphere(.55f,10,6);pop();}
    static void drawBuild(Build b){push();translate((float)b.x,(float)b.y,(float)b.z);glColor3f(.62f,.36f,.12f);if(b.type==0)cube(5,3,.3f);else if(b.type==1)ramp();else cube(5,.2f,5);pop();}
    static void item(Item q){push();translate((float)q.x,.65f,(float)q.z);if(q.type==0)glColor3f(.2f,.6f,1);if(q.type==1)glColor3f(.55f,.3f,.08f);if(q.type==2)glColor3f(.1f,.25f,1);if(q.type==3)glColor3f(.1f,.9f,.2f);rotate(45,0,1,0);cube(.7f,.7f,.7f);pop();}
    static void hud(){if(menu){menuScreen();return;} glDisable(GL_DEPTH_TEST);glMatrixMode(GL_PROJECTION);glPushMatrix();glLoadIdentity();glOrtho(0,W,H,0,-1,1);glMatrixMode(GL_MODELVIEW);glPushMatrix();glLoadIdentity();
        bar(25,25,270,24,health/100,.9f,.08f,.08f);bar(25,55,270,17,shield/100,.1f,.35f,1);
        glColor3f(1,1,1);glBegin(GL_LINES);glVertex2f(W/2-10,H/2);glVertex2f(W/2+10,H/2);glVertex2f(W/2,H/2-10);glVertex2f(W/2,H/2+10);glEnd();
        box(W-285,H-125,260,100,.03f,.04f,.07f);box(W-270,H-110,230,70,.12f,.12f,.14f);glColor3f(1,.8f,.1f);glBegin(GL_QUADS);glVertex2f(W-255,H-95);glVertex2f(W-45,H-95);glVertex2f(W-45,H-50);glVertex2f(W-255,H-50);glEnd();
        box(25,H-70,310,42,.03f,.04f,.07f);glColor3f(1,1,1);glBegin(GL_LINES);for(int i=0;i<5;i++){glVertex2f(40+i*55,H-58);glVertex2f(78+i*55,H-58);}glEnd();
        glMatrixMode(GL_MODELVIEW);glPopMatrix();glMatrixMode(GL_PROJECTION);glPopMatrix();glMatrixMode(GL_MODELVIEW);glEnable(GL_DEPTH_TEST);
    }
    static void lookAt(double ex,double ey,double ez,double tx,double ty,double tz){double dx=tx-ex,dy=ty-ey,dz=tz-ez,dist=Math.sqrt(dx*dx+dy*dy+dz*dz);rotate((float)Math.toDegrees(Math.asin(dy/dist)),1,0,0);rotate((float)Math.toDegrees(Math.atan2(dx,-dz)),0,1,0);translate((float)-ex,(float)-ey,(float)-ez);}
    static void menuScreen(){glDisable(GL_DEPTH_TEST);glMatrixMode(GL_PROJECTION);glPushMatrix();glLoadIdentity();glOrtho(0,W,H,0,-1,1);glMatrixMode(GL_MODELVIEW);glPushMatrix();glLoadIdentity();glColor3f(.035f,.07f,.13f);glBegin(GL_QUADS);v2(0,0);v2(W,0);v2(W,H);v2(0,H);glEnd();glColor3f(.08f,.45f,.95f);glBegin(GL_QUADS);v2(370,125);v2(910,125);v2(910,245);v2(370,245);glEnd();glColor3f(.08f,.55f,.98f);glBegin(GL_QUADS);v2(450,350);v2(830,350);v2(830,410);v2(450,410);glEnd();glColor3f(.08f,.12f,.18f);glBegin(GL_QUADS);v2(430,455);v2(850,455);v2(850,520);v2(430,520);glEnd();glMatrixMode(GL_MODELVIEW);glPopMatrix();glMatrixMode(GL_PROJECTION);glPopMatrix();glMatrixMode(GL_MODELVIEW);glEnable(GL_DEPTH_TEST);}
    static void v2(float x,float y){glVertex2f(x,y);}
    static void bar(float x,float y,float w,float h,double p,float r,float g,float b){box(x,y,w,h,.05f,.05f,.07f);glColor3f(r,g,b);glBegin(GL_QUADS);float q=(float)Math.max(0,Math.min(1,p));glVertex2f(x+2,y+2);glVertex2f(x+2+(w-4)*q,y+2);glVertex2f(x+2+(w-4)*q,y+h-2);glVertex2f(x+2,y+h-2);glEnd();}
    static void box(float x,float y,float w,float h,float r,float g,float b){glColor3f(r,g,b);glBegin(GL_QUADS);glVertex2f(x,y);glVertex2f(x+w,y);glVertex2f(x+w,y+h);glVertex2f(x,y+h);glEnd();}
    static void cube(float sx,float sy,float sz){float x=sx/2,y=sy/2,z=sz/2;glBegin(GL_QUADS);face(-x,-y,z,x,-y,z,x,y,z,-x,y,z);face(x,-y,-z,-x,-y,-z,-x,y,-z,x,y,-z);face(-x,-y,-z,-x,-y,z,-x,y,z,-x,y,-z);face(x,-y,z,x,-y,-z,x,y,-z,x,y,z);face(-x,y,z,x,y,z,x,y,-z,-x,y,-z);face(-x,-y,-z,x,-y,-z,x,-y,z,-x,-y,z);glEnd();}
    static void ramp(){glBegin(GL_TRIANGLES);v(-2.5f,0,-2.5f);v(2.5f,0,-2.5f);v(2.5f,3,2.5f);v(-2.5f,0,-2.5f);v(2.5f,3,2.5f);v(-2.5f,3,2.5f);glEnd();}
    static void pyramid(float b,float h){glBegin(GL_TRIANGLES);v(-b,0,-b);v(b,0,-b);v(0,h,0);v(b,0,-b);v(b,0,b);v(0,h,0);v(b,0,b);v(-b,0,b);v(0,h,0);v(-b,0,b);v(-b,0,-b);v(0,h,0);glEnd();}
    static void sphere(float r,int s,int t){for(int i=0;i<t;i++){double a=Math.PI*i/t-Math.PI/2,b=Math.PI*(i+1)/t-Math.PI/2;glBegin(GL_QUAD_STRIP);for(int j=0;j<=s;j++){double q=2*Math.PI*j/s;glVertex3d(r*Math.cos(a)*Math.cos(q),r*Math.sin(a),r*Math.cos(a)*Math.sin(q));glVertex3d(r*Math.cos(b)*Math.cos(q),r*Math.sin(b),r*Math.cos(b)*Math.sin(q));}glEnd();}}
    static void cylinder(float r,float h,int s){glBegin(GL_QUAD_STRIP);for(int i=0;i<=s;i++){double q=2*Math.PI*i/s;glVertex3d(r*Math.cos(q),0,r*Math.sin(q));glVertex3d(r*Math.cos(q),h,r*Math.sin(q));}glEnd();}
    static void v(float x,float y,float z){glVertex3f(x,y,z);}static void face(float...q){for(int i=0;i<12;i+=3)v(q[i],q[i+1],q[i+2]);}
    static void push(){glPushMatrix();}static void pop(){glPopMatrix();}static void translate(float x,float y,float z){glTranslatef(x,y,z);}static void rotate(float a,float x,float y,float z){glRotatef(a,x,y,z);}
    static void perspective(double f,float aspect,double n,double fa){double top=Math.tan(Math.toRadians(f)/2)*n,right=top*aspect;glFrustum(-right,right,-top,top,n,fa);}
}