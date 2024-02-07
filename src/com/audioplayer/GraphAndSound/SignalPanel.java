package com.audioplayer.GraphAndSound;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class SignalPanel extends JPanel {
    protected float[] tmpOut1 = null;    
    protected float[] tmpOut2 = null;    
    private Maximum[] maximums1 = new Maximum[32];
    private Maximum[] maximums2 = new Maximum[32];
    private volatile boolean painting = false;
    public void setTmpOut(float[] tmp, int channel)
    {
        if (painting) return;
        if (channel == 1)
        {
            this.tmpOut1 = new float[32];
            for (int i = 0; i < 32; ++i) 
            {
                this.tmpOut1[i] = Math.abs(tmp[i]);
            }
            this.repaint(new Rectangle(10, 20, 32 * 10, 30));
        }
        else
        {
            this.tmpOut2 = new float[32];
            for (int i = 0; i < 32; ++i) 
            {
                this.tmpOut2[i] = Math.abs(tmp[i]);
            }
            this.repaint(new Rectangle(310, 20, 320, 30));
        }
    }

    public SignalPanel()
    {
        this.setBackground(Color.LIGHT_GRAY);
        //this.setBorder(BorderFactory.createBevelBorder(0));
        this.setVisible(true);
        this.setSize(400, 200);
        this.setPreferredSize(new Dimension(583,100));
        for (int i = 0; i < 32; ++i)
        {
            maximums1[i] = new Maximum();
            maximums2[i] = new Maximum();
        }
    }    

    @Override
    protected void paintComponent(Graphics g) 
    {
        this.painting = true;      
        if (tmpOut1 != null) 
        {
            g.setColor(Color.lightGray);
            g.fillRect(10, 20, 320+320, 30);
            float max = 0;
            for (int i = 0; i < 32; i++)
            {
                if (tmpOut1[i] > max) max = tmpOut1[i];
            }
            for (int i = 0; i < 32; ++i)
            {
                g.setColor(Color.gray);
                if (max == 0)
                    g.fillRect(i * 9 + 10, 20, 5, 1);
                else
                {
                    int y = (int)(20 * tmpOut1[i] / max);
                    if (y > 20)
                    {
                        y=+1;
                    }
                    g.fillRect(i * 9 + 10, 20, 5, y);
                }
                Maximum maximum = this.maximums1[i];
                Returnable r = maximum.getReturnable(i, tmpOut1[i], max);
                g.setColor(r.color);
                g.fillRect(r.rect.x, r.rect.y, r.rect.width, r.rect.height);
            }
        }
        if (tmpOut2 != null)
        {
            g.setColor(Color.lightGray);
            g.fillRect(10 + 320, 20, 320, 30);
            float max = 0;
            for (int i = 0; i < 32; i++)
            {
                if (tmpOut2[i] > max) max = Math.abs(tmpOut2[i]);
            }
            for (int i = 0; i < 32; ++i)
            {
                g.setColor(Color.gray);
                if (max == 0)
                    g.fillRect(i * 9 + 10 + 295, 20, 5, 1);
                else
                {
                    int y = (int)(20 * Math.abs(tmpOut2[i]) / max);
                    g.fillRect(i * 9 + 10 + 295, 20, 5, y);
                }
                Maximum maximum = this.maximums2[i];
                Returnable r = maximum.getReturnable(i, Math.abs(tmpOut2[i]), max);
                g.setColor(r.color);
                g.fillRect(r.rect.x + 295, r.rect.y, r.rect.width, r.rect.height);
            }
        }
        this.painting = false;
    }
    
}

class Maximum
{
    
    float maximum = 0;
    float max = 0;
    int timer = 0;
    Logger logger = Logger.getLogger(GUISynthesiszer.class.getName());
    public Returnable getReturnable(int column, float tmp, float max)
    {
        Returnable ret = new Returnable();
        if (tmp > max) 
        {
            tmp++;
        }
        if (tmp > maximum) 
        {
            maximum = tmp;
            timer = 0;
            this.max = max;
        }
        ret.color = Color.darkGray;
        for (int i = 0; i < timer; i+=5)
        {
            //logger.info("timer=" + timer);
            if (i / 5 < 4)
            {
                ret.color = ret.color.brighter();
            }
            else
            {
                ret.color = Color.darkGray;
                timer = 0;
                maximum = 0;
            }
        }
        timer += 1;
        //logger.info("maximum:"+maximum+" max:" + max + " this.max:" + this.max + ";");
        ret.rect = new Rectangle(column  * 9 + 10, 20 + (int)(20 * maximum / this.max) 
                                , 5, 3);
        return ret;
    }    
}

class Returnable
{
    public Color color;
    public Rectangle rect;
}