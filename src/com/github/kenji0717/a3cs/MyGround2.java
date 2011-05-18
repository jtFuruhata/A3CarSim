package com.github.kenji0717.a3cs;

import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.linearmath.*;

import javax.vecmath.*;
import jp.sourceforge.acerola3d.a3.*;

//地面を表すクラス
public class MyGround2 extends A3RigidBody {
    public MyGround2(PhysicalWorld pw) throws Exception {
        //super(0.0,-50.0,0.0,pw);
        super(0.0,0.0,0.0,pw);
    }

    public A3Object makeA3Object() throws Exception {
        VRML vrml = new VRML("x-rzip:x-res:///res/stk_racetrack.a3!/racetrack.wrl");
        return vrml;
    }
    //地面用の剛体を作る
    public RigidBody makeRigidBody() {
        CollisionShape groundShape = Util.makeBvhTriangleMeshShape(a3.getNode());
        RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(0.0f, motionState, groundShape, new Vector3f());
        RigidBody body = new RigidBody(cInfo);
        return body;
    }
}