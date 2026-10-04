package com.jacobprojects.fortnitepe;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public final class FortnitePE {
    static long window; static final int W=1280,H=720;
    static double px=0,pz=0,yaw=0,pitch=0,health=100,wood=100; static boolean shooting,building;
    static final List<Bot> bots=new ArrayList<>(); static final List<Build> builds=new ArrayList<>();
    static final Random rng=new Random(7);

    record Bot(double x,double z,double hp) {}
    record Build(double x,double y,double z,int type) {}

    public static void main(String[] args){
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit()) throw new IllegalStateException("GLFW failed");
        glfwDefaultWindowHints(); glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,2); glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,1);
        window=glfwCreateWindow(W,H,"Fortnite PE — Performance Edition",0L,0L);
        if(window==0) throw new IllegalStateException("Window creation failed");
        glfwMakeContextCurrent(window); glfwSwapInterval(1); glfwShowWindow(window); GL.createCapabilities();
        glEnable(GL_DEPTH_TEST); glEnable(GL_CULL_FACE); glClearColor(.035f,.07f,.12f,1);
        for(int i=0;i<16;i++) bots.add(new Bot(rng.nextDouble()*100-50,rng.nextDouble()*100-50,100));
        glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_DISABLED);
        final double[] mx={0},my={0},lastx={W/2.0},lasty={H/2.0};
        glfwSetCursorPos(window,W/2,H/2);
        glfwSetCursorPosCallback(window,(w,x,y)->{mx[0]=x-lastx[0];my[0]=y-lasty[0];lastx[0]=x;lasty[0]=y;});
        glfwSetMouseButtonCallback(window,(w,b,a,m)->{if(b==GLFW_MOUSE_BUTTON_LEFT) shooting=a==GLFW_PRESS;if(b==GLFW_MOUSE_BUTTON_RIGHT) building=a==GLFW_PRESS;});
        glfwSetKeyCallback(window,(w,k,s,a,m)->{if(a==GLFW_PRESS){if(k==GLFW_KEY_ESCAPE)glfwSetWindowShouldClose(w,true); if(k==GLFW_KEY_F1||k==GLFW_KEY_F2)building=true;}});
        long last=System.nanoTime();
        while(!glfwWindowShouldClose(window)){
            long now=System.nanoTime(); float dt=Math.min(.033f,(now-last)/1_000_000_000f); last=now;
            yaw-=mx[0]*.0025; pitch=Math.max(-1.2,Math.min(1.2,pitch-my[0]*.0025)); mx[0]=my[0]=0;
            update(dt); render(); glfwSwapBuffers(window); glfwPollEvents();
        }
        glfwDestroyWindow(window);glfwTerminate();
    }

    static void update(float dt){
        double speed=glfwGetKey(window,GLFW_KEY_LEFT_SHIFT)==GLFW_PRESS?12:7;
        double fx=-Math.sin(yaw),fz=-Math.cos(yaw),rx=Math.cos(yaw),rz=-Math.sin(yaw);
        double x=0,z=0;if(glfwGetKey(window,GLFW_KEY_W)==GLFW_PRESS){x+=fx;z+=fz;}if(glfwGetKey(window,GLFW_KEY_S)==GLFW_PRESS){x-=fx;z-=fz;}
        if(glfwGetKey(window,GLFW_KEY_D)==GLFW_PRESS){x+=rx;z+=rz;}if(glfwGetKey(window,GLFW_KEY_A)==GLFW_PRESS){x-=rx;z-=rz;}
        double len=Math.hypot(x,z);if(len>0){px+=x/len*speed*dt;pz+=z/len*speed*dt;}
        if(shooting){for(int i=0;i<bots.size();i++){Bot b=bots.get(i);double dx=b.x-px,dz=b.z-pz;if(Math.hypot(dx,dz)<55){double hp=b.hp-35*dt;if(hp<=0)bots.remove(i--);else bots.set(i,new Bot(b.x,b.z,hp));break;}}}
        if(building&&glfwGetMouseButton(window,GLFW_MOUSE_BUTTON_LEFT)==GLFW_PRESS&&wood>=10){builds.add(new Build(px+fx*4,0,pz+fz*4,0));wood-=10;building=false;}
        for(int i=0;i<bots.size();i++){Bot b=bots.get(i);double dx=px-b.x,dz=pz-b.z,d=Math.hypot(dx,dz);if(d<25)health-=8*dt;else if(d>8)bots.set(i,new Bot(b.x+dx/d*2*dt,b.z+dz/d*2*dt,b.hp));}
    }

    static void render(){
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT); glMatrixMode(GL_PROJECTION);glLoadIdentity();perspective(70,(float)W/H,.1f,500);
        glMatrixMode(GL_MODELVIEW);glLoadIdentity();rotate((float)Math.toDegrees(pitch),1,0,0);rotate((float)Math.toDegrees(yaw),0,1,0);translate((float)-px,-3,(float)-pz);
        drawGround(); for(Bot b:bots){push();translate((float)b.x,1,(float)b.z);cube(.8f,2f,.8f,.15f,.55f,1f);pop();}
        for(Build b:builds){push();translate((float)b.x,1.5f,(float)b.z);cube(5,3,.25f,.45f,.25f,.08f);pop();}
        push();translate((float)px,1,(float)pz-2);cube(.7f,2,.7f,.15f,.35f,.8f);pop();
        glMatrixMode(GL_PROJECTION);glLoadIdentity();glOrtho(0,W,H,0,-1,1);glMatrixMode(GL_MODELVIEW);glLoadIdentity();glDisable(GL_DEPTH_TEST);
        glColor3f(1,1,1);glBegin(GL_LINES);glVertex2f(W/2-9,H/2);glVertex2f(W/2+9,H/2);glVertex2f(W/2,H/2-9);glVertex2f(W/2,H/2+9);glEnd();glEnable(GL_DEPTH_TEST);
    }
    static void drawGround(){glColor3f(.25f,.55f,.25f);glBegin(GL_QUADS);glVertex3f(-250,0,-250);glVertex3f(250,0,-250);glVertex3f(250,0,250);glVertex3f(-250,0,250);glEnd();}
    static void cube(float sx,float sy,float sz,float r,float g,float b){glColor3f(r,g,b);float x=sx/2,y=sy/2,z=sz/2;glBegin(GL_QUADS);
        face(-x,-y,z,x,-y,z,x,y,z,-x,y,z);face(x,-y,-z,-x,-y,-z,-x,y,-z,x,y,-z);face(-x,-y,-z,-x,-y,z,-x,y,z,-x,y,-z);face(x,-y,z,x,-y,-z,x,y,-z,x,y,z);face(-x,y,z,x,y,z,x,y,-z,-x,y,-z);face(-x,-y,-z,x,-y,-z,x,-y,z,-x,-y,z);glEnd();}
    static void face(float...v){for(int i=0;i<12;i+=3)glVertex3f(v[i],v[i+1],v[i+2]);}
    static void push(){glPushMatrix();}static void pop(){glPopMatrix();}static void translate(float x,float y,float z){glTranslatef(x,y,z);}static void rotate(float a,float x,float y,float z){glRotatef(a,x,y,z);}
    static void perspective(float fov,float aspect,float near,float far){float top=(float)Math.tan(Math.toRadians(fov)/2)*near;float right=top*aspect;glFrustum(-right,right,-top,top,near,far);}
}