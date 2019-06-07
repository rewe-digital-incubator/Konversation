// This is a generated file. Not intended for manual editing.
package org.rewedigital.konversation.editor.impl;

import java.util.List;

import org.jetbrains.annotations.*;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;

import static org.rewedigital.konversation.editor.psi.KonversationTypes.*;

import com.intellij.extapi.psi.ASTWrapperPsiElement;

import org.rewedigital.konversation.editor.psi.*;

public class KonversationPromptBlockImpl extends ASTWrapperPsiElement implements KonversationPromptBlock {

    public KonversationPromptBlockImpl(@NotNull ASTNode node) {
        super(node);
    }

    public void accept(@NotNull KonversationVisitor visitor) {
        visitor.visitPromptBlock(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof KonversationVisitor) {
            accept((KonversationVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    @Override
    @NotNull
    public List<KonversationBlockDelimitter> getBlockDelimitterList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, KonversationBlockDelimitter.class);
    }

    @Override
    @NotNull
    public List<KonversationBlockPart> getBlockPartList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, KonversationBlockPart.class);
    }
}