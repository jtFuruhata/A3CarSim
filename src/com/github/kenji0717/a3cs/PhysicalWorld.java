package com.github.kenji0717.a3cs;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.*;
import java.util.*;
import javax.vecmath.*;
import jp.sourceforge.acerola3d.a3.*;

//物理計算をしてくれるクラス
public class PhysicalWorld implements Runnable {
    static int MAX_PROXIES = 1024;
    DiscreteDynamicsWorld dynamicsWorld;
    ArrayList<A3CollisionObject> objects = new ArrayList<A3CollisionObject>();
    ArrayList<A3CollisionObject> newObjects = new ArrayList<A3CollisionObject>();
    ArrayList<A3CollisionObject> delObjects = new ArrayList<A3CollisionObject>();
    A3Window window;
    ArrayList<CollisionListener> collisionListeners = new ArrayList<CollisionListener>();

    //物理世界の初期化
    public PhysicalWorld() {
    	window = new A3Window(600,600);
        CollisionConfiguration collisionConfiguration =
                new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher =
                new CollisionDispatcher(collisionConfiguration);
        /*
        Vector3f worldAabbMin = new Vector3f(-10000,-10000,-10000);
        Vector3f worldAabbMax = new Vector3f(10000,10000,10000);
        int maxProxies = MAX_PROXIES;
        AxisSweep3 overlappingPairCache =
                new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
        SequentialImpulseConstraintSolver solver =
                new SequentialImpulseConstraintSolver();
        */
        BroadphaseInterface overlappingPairCache = new DbvtBroadphase();
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();

        dynamicsWorld =
                new DiscreteDynamicsWorld(dispatcher,overlappingPairCache,
                                          solver,collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0,-10,0));

        Thread t = new Thread(this);
        t.start();
    }

    //新規の剛体を加える
    public void add(A3CollisionObject rb) {
        synchronized (newObjects) {
            newObjects.add(rb);
        }
    }

    //既存の剛体を削除
    public void del(A3CollisionObject rb) {
        synchronized (delObjects) {
            delObjects.add(rb);
        }
    }

    //物理計算を進める処理
    //座標を変更するのがちょっとやっかい
    public void run() {
        while (true) {
System.out.println("gaha1");
            synchronized (newObjects) {
                for (A3CollisionObject co : newObjects) {
                    //if (rb instanceof MyCar)
                    //    dynamicsWorld.removeVehicle(((MyCar)rb).motion.vehicle);
                    if (co.coType==COType.GHOST) {
                        dynamicsWorld.addCollisionObject(co.body,co.group,co.mask);
                    } else {
                        dynamicsWorld.addRigidBody((RigidBody)co.body,co.group,co.mask);
                    }
                    if (window!=null)
                        window.add(co.a3);
                    objects.add(co);
                }
                newObjects.clear();
            }
System.out.println("gaha2");
            synchronized (delObjects) {
                for (A3CollisionObject co : delObjects) {
                    //if (rb instanceof MyCar)
                    //    dynamicsWorld.addVehicle(((MyCar)rb).motion.vehicle);
                    if (co.coType==COType.GHOST) {
                        dynamicsWorld.removeCollisionObject(co.body);
                    } else {
                        dynamicsWorld.removeRigidBody((RigidBody)co.body);
                    }
                    if (window!=null)
                        window.del(co.a3);
                    objects.remove(co);
                }
                delObjects.clear();
            }
System.out.println("gaha3");

            for (A3CollisionObject co : objects) {
                if (co.locRequest==null)
                    continue;
                Transform t = new Transform();
                t.origin.set(co.locRequest);
                co.motionState.setWorldTransform(t);
                if (co.coType==COType.DYNAMIC)
                    co.changeCOType(COType.KINEMATIC_TEMP);
            }

            //ここで物理計算
            dynamicsWorld.stepSimulation(1.0f/30.0f,10);
            //dynamicsWorld.stepSimulation(1.0f/30.0f,2);

System.out.println("-----gaha-----");
/*
            //衝突
            int numManifolds = dynamicsWorld.getDispatcher().getNumManifolds();
            for (int ii=0;ii<numManifolds;ii++) {
                PersistentManifold contactManifold = dynamicsWorld.getDispatcher().getManifoldByIndexInternal(ii);
                CollisionObject obA = (CollisionObject)contactManifold.getBody0();
//System.out.println("obA:"+obA.getUserPointer().getClass().getName());
                CollisionObject obB = (CollisionObject)contactManifold.getBody1();
//System.out.println("obB:"+obB.getUserPointer().getClass().getName());
                int numContacts = contactManifold.getNumContacts();
//System.out.println("numContacts:"+numContacts);
                for (int j=0;j<numContacts;j++) {
                    ManifoldPoint pt = contactManifold.getContactPoint(j);
                    if (pt.getDistance()<0.0f) {
                        System.out.println("-----------------");
                        System.out.println("ii:"+ii+"    j:"+j);
                        System.out.println("getLifeTime:"+pt.getLifeTime());
                        System.out.println("PositionWorldOnA:"+pt.positionWorldOnA);
                        System.out.println("PositionWorldOnB:"+pt.positionWorldOnB);
                        System.out.println("normalWorldOnB:"+pt.normalWorldOnB);
                        System.out.println("-----------------");
                    }
                }

                //ロックしすぎ？
                synchronized (collisionListeners) {
                    for (CollisionListener cl : collisionListeners) {
                        cl.collided(((A3CollisionObject)obA.getUserPointer()),((A3CollisionObject)obB.getUserPointer()));
                    }
                }
            }
*/
System.out.println("gaha4");

            for (A3CollisionObject co : objects) {
                if (co.locRequest==null)
                    continue;
                if (co.coType==COType.KINEMATIC_TEMP){
                    co.changeCOType(COType.DYNAMIC);
                }
                co.locRequest=null;
            }

System.out.println("gaha5");
            for (A3CollisionObject co : objects) {
                if (co.velRequest==null)
                    continue;
                if (co.coType!=COType.GHOST) {
                    ((RigidBody)co.body).setLinearVelocity(co.velRequest);
                }
                co.velRequest=null;
            }
System.out.println("gaha6");
            try{Thread.sleep(33);}catch(Exception e){;}
        }
    }
    public void addCollisionListener(CollisionListener cl) {
        synchronized (collisionListeners) {
            collisionListeners.add(cl);
        }
    }
    public void removeCollisionListener(CollisionListener cl) {
        synchronized (collisionListeners) {
            collisionListeners.remove(cl);
        }
    }
}
