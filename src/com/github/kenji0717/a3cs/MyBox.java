package com.github.kenji0717.a3cs;

import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.linearmath.*;
import javax.vecmath.*;
import jp.sourceforge.acerola3d.a3.*;

//立方体を表すクラス
public class MyBox extends A3RigidBody {
    public MyBox(double x,double y,double z,PhysicalWorld pw) throws Exception {
        super(x,y,z,pw);
    }

    public A3Object makeA3Object() throws Exception {
        return new Action3D("x-res:///res/SimpleBox.a3");
    }
    //立方体の剛体を作る
    public RigidBody makeRigidBody() {
        CollisionShape shape = new BoxShape(new Vector3f(1.0f,1.0f,1.0f));
        Vector3f localInertia = new Vector3f(0,0,0);
        shape.calculateLocalInertia(1.0f,localInertia);
        RigidBodyConstructionInfo rbcInfo =
                new RigidBodyConstructionInfo(1.0f,motionState,
                                              shape,localInertia);
        RigidBody rb = new RigidBody(rbcInfo);
        return rb;
    }
}